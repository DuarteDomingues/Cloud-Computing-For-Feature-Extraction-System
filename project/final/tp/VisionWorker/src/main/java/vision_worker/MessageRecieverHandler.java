package vision_worker;


import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MessageRecieverHandler implements MessageReceiver {
    public MessageRecieverHandler() {
        super();
    }

    public void receiveMessage(PubsubMessage msg, AckReplyConsumer ackReply) {
        try {
            System.out.println("Message(Id:" + msg.getMessageId() + " Data:" + msg.getData().toStringUtf8() + ")");
            var gscAtt = msg.getAttributesMap();
            System.out.println("Atributes : " + gscAtt);

            var gcsPath = "gs://" + gscAtt.get("targetBucket") + "/" + gscAtt.get("filename");


            List<AnnotateImageRequest> requests = new ArrayList<>();
            var image = getContentFromCloudStorage(gcsPath);
            Feature feat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
            AnnotateImageRequest request =
                    AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(image).build();
            requests.add(request);

            var labelsDict = new HashMap<String, String>();

            // Initialize client that will be used to send requests. This client only needs to be created
            // once, and can be reused for multiple requests. After completing all of your requests, call
            // the "close" method on the client to safely clean up any remaining background resources.
            try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
                BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
                List<AnnotateImageResponse> responses = response.getResponsesList();

                for (AnnotateImageResponse res : responses) {
                    if (res.hasError()) {
                        System.out.format("Error: %s%n", res.getError().getMessage());
                        ackReply.nack();
                        return;
                    }

                    // For full list of available annotations, see http://g.co/cloud/vision/docs
                    for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
                        System.out.println("Description " + annotation.getDescription() + " Score " + annotation.getScore());
                        //Guarda num dicionario uma feature deteda e o valor de certeza dela
                        labelsDict.put(annotation.getDescription(), String.valueOf(annotation.getScore()));
                    }
                }
            }

            placeInPubSub(gscAtt.get("identifier"), labelsDict);

            ackReply.ack();

        } catch (Exception exception) {
            // se houver uma excepcao dá Not Ack e a mensagem é recebida novamente
            exception.printStackTrace();
            ackReply.nack();
        }
    }


    private void placeInPubSub(String msgTxt, HashMap<String, String> dict) throws IOException, ExecutionException, InterruptedException {
        TopicName tName = TopicName.ofProjectTopicName(GlobalVars.DEFAULT_PROJECT, GlobalVars.TRANSLATION_TOPIC);
        Publisher publisher = Publisher.newBuilder(tName).build();

        ByteString msgData = ByteString.copyFromUtf8(msgTxt);
        // Por cada mensagem
        PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                .setData(msgData)
                .putAllAttributes(dict)
                .build();

        ApiFuture<String> future = publisher.publish(pubsubMessage);
        String msgID = future.get();
        System.out.println("Message Published with ID=" + msgID);
        // No fim de enviar as mensagens
        publisher.shutdown();
    }

    private Image getContentFromCloudStorage(String gcsPath) {
        ImageSource imgSource = ImageSource.newBuilder().setGcsImageUri(gcsPath).build();
        return Image.newBuilder().setSource(imgSource).build();

    }

    /*
================================= MENSAGENS ENVIADAS ENTRE AS DIFERENTES PARTES DO PROJETO =================================

            ========= Server to PUB/SUB(Vision Worker) (depois da imagem ser armazenada na Cloud)

    Target topic = topicworkers
    Message(Id:<Random long> Data:<Img hash value>)
    Atributes :    {targetBucket=<bucket onde foi guardada a imagem>,
        identifier=<Img hash value>,
                filename=<filename na máquina de destino>
                contentType=image/jpeg}

========= Server (Vision Worker) to Cloud Function (Translation Worker) (depois de serem obtidos os labels)
    Target topic = labelTranslationworkers
    Message(Id:<Random long> Data:<Img hash value>)
    Atributes :    {label-<language>=[<Score do label>]} // 0 ou mais elementos na lista


*/


}

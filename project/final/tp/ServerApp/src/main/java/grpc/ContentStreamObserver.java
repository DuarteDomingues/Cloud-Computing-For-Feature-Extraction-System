package grpc;

import com.google.api.core.ApiFuture;
import com.google.cloud.WriteChannel;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import firestore.FirestoreManipulator;
import io.grpc.stub.StreamObserver;
import main.GlobalVars;
import tpFinalService.Content;
import tpFinalService.StoredContentID;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class ContentStreamObserver implements StreamObserver<Content> {

    private final Storage storage;
    private final StreamObserver<StoredContentID> responseObserver;

    private WriteChannel writer;
    private String blobName;
    private BlobInfo blobInfo;
    private String contentType;

    private final FirestoreManipulator firestoreService;

    public ContentStreamObserver(Storage storage, StreamObserver<StoredContentID> responseObserver, FirestoreManipulator firestoreService) {
        this.storage = storage;
        this.responseObserver = responseObserver;
        this.blobName = null;
        this.blobInfo = null;

        this.firestoreService = firestoreService;


    }

    @Override
    public void onNext(Content contents) {
        String blobName = contents.getFilename();
        System.out.println("CONA");
        if (this.writer == null) {
            //Na primeira vez guarda o name e outras cenas, na segunda vez segue em frente
            this.blobName = blobName;
            BlobId blobId = BlobId.of(GlobalVars.DEFAULT_BUCKET, blobName);
            this.contentType = contents.getContentType();
            this.blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();
            this.writer = storage.writer(blobInfo);
        }
        try {
            this.writer.write(contents.getFileBlockBytes().asReadOnlyByteBuffer());
        } catch (IOException exception) {
            exception.printStackTrace();
            responseObserver.onError(exception);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Error on ContentStream Recieved");
        throwable.printStackTrace();
    }


    @Override
    public void onCompleted() {
        System.out.println("Finished recieving transmission");

        try {
            var hash = GlobalVars.DEFAULT_BUCKET + "$" + this.blobName;

            System.out.println("HASH " + hash);
            var result = StoredContentID.newBuilder().
                    setIdentifier(hash).
                    build();

            var metadata = new HashMap<String, String>() {{
                put("identifier", hash);
                put("filename", blobName);
                put("contentType", contentType);
                put("targetBucket", GlobalVars.DEFAULT_BUCKET);
            }};

            System.out.println("Firestore ");
            firestoreService.placeInFirestoreDict(hash, metadata);

            System.out.println("PUBSUB ");
            placeInPubSub(hash, metadata);

            responseObserver.onNext(result);
            responseObserver.onCompleted();

            //firestoreService.firestoreClose();
            writer.close();

        } catch (Exception exception) {
            exception.printStackTrace();
            responseObserver.onError(exception);
        }
    }

    private void placeInPubSub(String msgTxt, HashMap<String, String> dict) throws IOException, ExecutionException, InterruptedException {

        TopicName tName = TopicName.ofProjectTopicName(GlobalVars.DEFAULT_PROJECT, GlobalVars.DEFAULT_TOPIC);
        System.out.println("BUILDER");
        Publisher publisher = Publisher.newBuilder(tName).build();

        ByteString msgData = ByteString.copyFromUtf8(msgTxt);
        // Por cada mensagem
        PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                .setData(msgData)
                .putAllAttributes(dict)
                .build();
        System.out.println("publish");

        ApiFuture<String> future = publisher.publish(pubsubMessage);
        String msgID = future.get();
        System.out.println("Message Published with ID=" + msgID);
        // No fim de enviar as mensagens
        publisher.shutdown();
    }
}

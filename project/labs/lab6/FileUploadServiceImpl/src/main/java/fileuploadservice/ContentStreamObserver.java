package fileuploadservice;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.WriteChannel;
import com.google.cloud.firestore.*;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import io.grpc.stub.StreamObserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class ContentStreamObserver implements StreamObserver<Contents> {
    private static final String DEFAULT_PROJECT = "robust-seat-309417";
    private static final String DEFAULT_BUCKET = "isel-cn-g14";
    private static final String DEFAULT_COLLECTION = "lab6_cn_g14";
    private static final String DEFAULT_TOPIC = "cn-g14";

    private final Storage storage;
    private final StreamObserver<Result> responseObserver;
    private final Firestore db;
    private CollectionReference currentCollection;
    private WriteChannel writer;
    private String blobName;
    private BlobInfo blobInfo;
    private int numOfChunks;
    private String contentType;

    public ContentStreamObserver(Storage storage, StreamObserver<Result> responseObserver, String pathToCredentials) throws IOException {
        this.storage = storage;
        this.responseObserver = responseObserver;
        this.blobName = null;
        this.blobInfo = null;

        GoogleCredentials credentials = null;
        if (pathToCredentials != null) {
            InputStream serviceAccount = new FileInputStream(pathToCredentials);
            credentials = GoogleCredentials.fromStream(serviceAccount);
        } else {
            credentials = GoogleCredentials.getApplicationDefault();
        }


        FirestoreOptions options = FirestoreOptions
                .newBuilder().setCredentials(credentials).build();
        db = options.getService();

        currentCollection = db.collection(DEFAULT_COLLECTION);
    }

    @Override
    public void onNext(Contents contents) {
        String blobName = contents.getFilename();
        if (writer == null) {
            //Na primeira vez guarda o name e outras cenas, na segunda vez segue em frente
            this.blobName = blobName;
            BlobId blobId = BlobId.of(DEFAULT_BUCKET, blobName);
            this.contentType = contents.getContentType();
            this.blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();
            this.writer = storage.writer(blobInfo);
        }
        try {
            this.writer.write(contents.getFileBlockBytes().asReadOnlyByteBuffer());
            numOfChunks++;
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
            writer.close();
            var hash = this.DEFAULT_BUCKET + this.blobName;
            Result result = Result.newBuilder().
                    setNumBlocksReceived(numOfChunks).
                    setHashId(hash).
                    build();

            //place in firebase o dict
            var newEntryToFirebase = new HashMap<String, String>() {{
                put("name", blobName);
                put("numChunks", numOfChunks + "");
                put("type", contentType);
            }};

            placeInFirestoreMetadata(hash, newEntryToFirebase);
            placeInPubSub(hash, newEntryToFirebase);
            responseObserver.onNext(result);
            responseObserver.onCompleted();
            db.close();

        } catch (Exception exception) {
            exception.printStackTrace();
            responseObserver.onError(exception);
        }
    }

    private void placeInPubSub(String msgTxt, HashMap<String, String> dict) throws IOException, ExecutionException, InterruptedException {
        TopicName tName = TopicName.ofProjectTopicName(DEFAULT_PROJECT, DEFAULT_TOPIC);
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

    private void placeInFirestoreMetadata(String hash, HashMap<String, String> newEntryToFirebase) {
        DocumentReference docRef = currentCollection.document(hash);
        ApiFuture<WriteResult> resultApiFuture = docRef.create(newEntryToFirebase);
    }
}

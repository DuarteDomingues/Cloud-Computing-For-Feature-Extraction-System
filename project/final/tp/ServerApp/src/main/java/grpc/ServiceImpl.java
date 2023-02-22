package grpc;

import com.google.cloud.storage.Storage;
import firestore.FirestoreManipulator;
import io.grpc.stub.StreamObserver;
import tpFinalService.Content;
import tpFinalService.StoredContentID;
import tpFinalService.tpServiceGrpc;

public class ServiceImpl extends tpServiceGrpc.tpServiceImplBase {
    private final FirestoreManipulator firestoreManipulator;
    private final Storage storage;


    public ServiceImpl(Storage storage, FirestoreManipulator firestoreManipulator) {
        super();
        this.storage = storage;
        this.firestoreManipulator = firestoreManipulator;
    }

    @Override
    public StreamObserver<Content> sendFileBlocks(StreamObserver<StoredContentID> responseObserver) {
        return new ContentStreamObserver(storage, responseObserver, firestoreManipulator);
    }
}

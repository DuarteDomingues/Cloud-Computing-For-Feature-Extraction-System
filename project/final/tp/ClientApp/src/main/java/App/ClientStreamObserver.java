package App;

import io.grpc.stub.StreamObserver;
import tpFinalService.StoredContentID;

import java.util.ArrayList;
import java.util.List;

public class ClientStreamObserver implements StreamObserver<StoredContentID> {
    private boolean isCompleted = false;
    private boolean success = false;

    public boolean OnSuccesss() {
        return success;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    @SuppressWarnings("CanBeFinal")
    List<StoredContentID> rplys = new ArrayList<>();

    @Override
    public void onNext(StoredContentID result) {
        rplys.add(result);
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Error on call:" );
        throwable.printStackTrace();
        isCompleted = true;
        success = false;
    }

    @Override
    public void onCompleted() {
        System.out.println("Stream completed");
        isCompleted = true;
        success = true;

        System.out.printf("Sent image with identifier:%s%n", rplys.get(rplys.size()-1).getIdentifier());
    }
}

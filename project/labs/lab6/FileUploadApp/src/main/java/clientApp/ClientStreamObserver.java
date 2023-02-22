package clientApp;

import fileuploadservice.Result;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;

public class ClientStreamObserver implements StreamObserver<Result> {
    private boolean isCompleted = false;
    private boolean success = false;

    public boolean OnSuccesss() {
        return success;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    List<Result> rplys = new ArrayList<Result>();

    @Override
    public void onNext(Result result) {
        rplys.add(result);
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Error on call:" + throwable.getMessage());
        isCompleted = true;
        success = false;
    }

    @Override
    public void onCompleted() {
        System.out.println("Stream completed");
        isCompleted = true;
        success = true;

        System.out.printf("Sent %d blocks with hashID %s", rplys.get(rplys.size()-1).getNumBlocksReceived(), rplys.get(rplys.size()-1).getHashId());
    }
}

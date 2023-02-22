package clientapp;

import io.grpc.stub.StreamObserver;
import rpcstubs.Reply;

import java.util.ArrayList;
import java.util.List;

public class ClientStreamObserver implements StreamObserver<Reply> {
    private boolean isCompleted=false;
    private boolean success=false;
    public boolean OnSuccesss() {
        return success;
    }

    public boolean isCompleted() {
        return isCompleted;
    }
    List<Reply> rplys = new ArrayList<Reply>();
    public List<Reply> getReplays() {
        return rplys;
    }
    @Override
    public void onNext(Reply reply) {
        System.out.println("Reply ("+reply.getRplyID()+"):"+reply.getTxt());
        rplys.add(reply);
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Error on call:"+throwable.getMessage());
        isCompleted=true; success=false;
    }

    @Override
    public void onCompleted() {
        System.out.println("Stream completed");
        isCompleted=true; success=true;
    }
}

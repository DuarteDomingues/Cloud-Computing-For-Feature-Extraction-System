package serverapp;

import io.grpc.stub.StreamObserver;
import rpcstubs.Reply;
import rpcstubs.Request;

public class ServerStreamObserverC3 implements StreamObserver<Request> {

    StreamObserver<Reply> sFinalreply;
    String finalText="";

    public ServerStreamObserverC3(StreamObserver<Reply> sreplies) {
        this.sFinalreply=sreplies;
    }

    @Override
    public void onNext(Request request) {
        // More one request
        finalText+=request.getTxt()+":";

    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onCompleted() {
        Reply rply = Reply.newBuilder().setRplyID(9999).setTxt(finalText.toUpperCase()).build();
        sFinalreply.onNext(rply);
        sFinalreply.onCompleted();

    }
}

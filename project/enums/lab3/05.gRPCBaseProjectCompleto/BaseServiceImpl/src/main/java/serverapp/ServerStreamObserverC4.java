package serverapp;

import io.grpc.stub.StreamObserver;
import rpcstubs.Reply;
import rpcstubs.Request;

public class ServerStreamObserverC4 implements StreamObserver<Request> {

    StreamObserver<Reply> sreplies;

    public ServerStreamObserverC4(StreamObserver<Reply> sreplies) {
        this.sreplies=sreplies;
    }

    @Override
    public void onNext(Request request) {
        // More one request to process and one reply
        Reply rply = Reply.newBuilder().setRplyID(request.getReqID()).setTxt(request.getTxt().toUpperCase()).build();
        sreplies.onNext(rply);
        // pode-se armazenar múltiplos pedidos e só serem respondidos
        // quando o cliente fizer OnCompleted
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onCompleted() {
        // processar eventuais mensagens de pedido recebidas em OnNext
        // e responder com uma ou mais respostas
        // splies.OnNext(OnlyReply);
        sreplies.onCompleted();
    }
}

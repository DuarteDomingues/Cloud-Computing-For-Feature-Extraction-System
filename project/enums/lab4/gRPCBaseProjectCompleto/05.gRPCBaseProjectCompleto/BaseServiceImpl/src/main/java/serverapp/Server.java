package serverapp;

import com.google.protobuf.Timestamp;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import rpcstubs.*;
import rpcstubs.News;
import rpcstubs.Void;

import java.util.Scanner;

public class Server extends BaseServiceGrpc.BaseServiceImplBase {

    private static int svcPort=8000;
    public static void main(String[] args) {
        try {
            if (args.length > 0) svcPort=Integer.parseInt(args[0]);
            io.grpc.Server svc = ServerBuilder
                    .forPort(svcPort)
                    .addService(new Server())
                    .build();
            svc.start();
              System.out.println("Server started, listening on " + svcPort);
              Scanner scan=new Scanner(System.in); scan.nextLine();
            svc.shutdown();
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void case1(Request request, StreamObserver<Reply> responseObserver) {
        System.out.println("case1 called");
        Reply rply = Reply.newBuilder().setRplyID(request.getReqID()).setTxt(request.getTxt().toUpperCase()).build();
        try {
            Thread.sleep(2*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        responseObserver.onNext(rply);
        responseObserver.onCompleted();
    }

    @Override
    public void case2(Request request, StreamObserver<Reply> responseObserver) {
        System.out.println("case2 called");
        for (int i=0; i < request.getReqID(); i++) {
            Reply rply = Reply.newBuilder().setRplyID(request.getReqID()).setTxt(request.getTxt().toUpperCase()+" "+i).build();
            responseObserver.onNext(rply);
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Request> case3(StreamObserver<Reply> responseObserver) {
        System.out.println("case3 called");
        ServerStreamObserverC3 reqs=new ServerStreamObserverC3(responseObserver);
        return reqs;
    }

    @Override
    public StreamObserver<Request> case4(StreamObserver<Reply> responseObserver) {
        System.out.println("case4 called");
        ServerStreamObserverC4 reqs=new ServerStreamObserverC4(responseObserver);
        return reqs;
    }

    @Override
    public void pingServer(Void request, StreamObserver<Reply> responseObserver) {
        System.out.println("pingServer called");
        Reply rply = Reply.newBuilder().setRplyID(0).setTxt("Server is alive").build();
        try {
            Thread.sleep(5*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        responseObserver.onNext(rply);
        responseObserver.onCompleted();
    }

    @Override
    public void publishNews(News request, StreamObserver<Void> responseObserver) {
        System.out.println("publishNews called");
        Timestamp ts=request.getTs();
        System.out.println("News timestamp:"+ts.getSeconds());
        responseObserver.onNext(Void.newBuilder().build());
        responseObserver.onCompleted();
    }
}

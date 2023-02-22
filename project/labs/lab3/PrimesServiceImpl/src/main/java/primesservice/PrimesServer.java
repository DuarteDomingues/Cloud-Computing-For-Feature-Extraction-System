package primesservice;


import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Scanner;

//faz extend do contratoGRPC
public class PrimesServer extends PrimesServiceGrpc.PrimesServiceImplBase {

    private static int svcPort = 80;

    public static void main(String[] args) {
        try {
            if(args.length==1){
                svcPort = Integer.parseInt(args[0]);
            }

            io.grpc.Server svc = ServerBuilder.forPort(svcPort).addService(new PrimesServer()).build();
            svc.start();
            System.out.println("Server iniciado");
            Scanner scan = new Scanner((System.in));
            scan.nextLine();
            svc.shutdown();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    //Quando o cliente chama o metodo isAlive, servidor faz isto:
    @Override
    public void isAlive(Void request, StreamObserver<Text> responseObserver) {
        Text text = Text.newBuilder().setMsg("1").build();
        System.out.println("Is alive called");
        responseObserver.onNext(text);
        responseObserver.onCompleted();
    }
    //Quando o cliente chama o metodo findPrimes, servidor faz isto
    @Override
    public void findPrimes(PrimesInterval request, StreamObserver<Prime> responseObserver) {
        try {
            System.out.println("Find Primes called");
            int current = request.getStartNum();
            while (current < request.getEndNum()) {
                if (isPrime(current)) {
                    Prime p = Prime.newBuilder().
                            setPrime(current).build();
                    Thread.sleep(200); //Simular processamento longo
                    responseObserver.onNext(p);
                }
                current++;
            }
            responseObserver.onCompleted();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean isPrime(int num) {
        if (num <= 1) return false;
        if (num < 4) return true;
        if (num % 2 == 0) return false;
        for (int i = 3; i <= Math.sqrt(num); i += 2) {
            if (num % i == 0) return false;
        }
        return true;
    }

}

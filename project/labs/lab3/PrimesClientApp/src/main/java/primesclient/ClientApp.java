package primesclient;


import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import primesservice.Prime;
import primesservice.PrimesInterval;
import primesservice.PrimesServiceGrpc;
import primesservice.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class ClientApp {

    private static String svcIP = "localhost";
    private static int svcPort = 80;

    private static ManagedChannel channel;
    private static PrimesServiceGrpc.PrimesServiceBlockingStub blockingStub;
    private static PrimesServiceGrpc.PrimesServiceStub noBlockStub;
    private static PrimesServiceGrpc.PrimesServiceFutureStub futStub;

    public static void main(String[] args) {
        try {
            if(args.length ==2){
                svcIP = args[0];
                svcPort = Integer.parseInt(args[1]);
            }

            channel = ManagedChannelBuilder.forAddress(svcIP, svcPort).usePlaintext().build();

            blockingStub = PrimesServiceGrpc.newBlockingStub(channel);
            noBlockStub = PrimesServiceGrpc.newStub(channel);
            futStub = PrimesServiceGrpc.newFutureStub(channel);

            ClientApp app = new ClientApp();
            if (blockingStub.isAlive(null) != null) {
                app.nonBlockingCall(1, 500, 5);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void blockingCall(int start, int end) {
        var primesInterval = PrimesInterval.newBuilder()
                .setStartNum(start)
                .setEndNum(end)
                .build();

        var primes = blockingStub.findPrimes(primesInterval);
        var valMsg = Text.newBuilder().setMsg("1").build();

        if (blockingStub.isAlive(null).equals(valMsg)) {
            System.out.println("Primes Received\n");
            for (Iterator<Prime> it = primes; it.hasNext(); ) {
                Prime prime = it.next();
                System.out.print(prime.getPrime() + ",");
            }
        }
    }

    public void nonBlockingCall(int start, int end, int numIntervals) throws InterruptedException {
        var intervals = new PrimesInterval[numIntervals];
        var step = (end - start) / numIntervals;

        for (int i = 0; i < intervals.length; i++) {
            start = i * step;
            end = (i + 1) * step;

            var primesInterval = PrimesInterval.newBuilder()
                    .setStartNum(start)
                    .setEndNum(end)
                    .build();

            intervals[i] = primesInterval;
        }

        var observer = new ClientStreamObserver();

        for (int i = 0; i < numIntervals; i++) {
            noBlockStub.findPrimes(intervals[i], observer);
        }


        while (!observer.isCompleted()) {
            Thread.sleep(1000);
            System.out.println("Cliente activo");
        }


        var primes = new ArrayList<Prime>();

        if (observer.OnSuccesss()) {
            primes = observer.getPrimes();
            System.out.println("\nPrimes Received");
            for (Prime p : primes) {
                System.out.print(p.getPrime() + ",");
            }
            System.out.println("\n");
        }
        Collections.sort(primes, new Comparator<Prime>() {
            @Override
            public int compare(Prime p1, Prime p2) {
                return p1.getPrime() - p2.getPrime();
            }
        });
        System.out.println("Sorted");
        for (Prime p : primes) {
            System.out.print(p.getPrime() + ",");
        }
    }
}

package primesclient;

import io.grpc.stub.StreamObserver;
import primesservice.Prime;

import java.util.ArrayList;

public class ClientStreamObserver implements StreamObserver<Prime> {
    private boolean isDone = false;
    private boolean success = false;
    private ArrayList primes = new ArrayList<Prime>();

    public boolean OnSuccesss() {
        return success;
    }

    public boolean isCompleted() {
        return isDone;
    }

    public ArrayList<Prime> getPrimes() {
        return primes;
    }

    @Override
    public void onNext(Prime prime) {
        //System.out.println("Received " + prime.toString());
        primes.add(prime);
    }

    @Override
    public void onError(Throwable throwable) {
        System.err.println(throwable.getStackTrace());
        isDone = true;
        success = false;
        System.exit(0);
    }

    @Override
    public void onCompleted() {
        System.out.println("Done receiving primes on this thread");
        isDone = true;
        success = true;
    }
}
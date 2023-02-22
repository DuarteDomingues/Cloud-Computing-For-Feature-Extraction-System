import java.rmi.RemoteException;

public class PrimeWorker extends Thread {
    private final int start;
    private final int end;
    private int current;
    private final ICallback callback;

    public PrimeWorker(int start, int end, ICallback callback) {
        this.start = start;
        this.end = end;
        this.current = start;
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            while (current < end) {
                if (isPrime(current)) {
                    //Simular processamento longo
                    Thread.sleep(1000);
                    this.callback.nextPrime(current);
                }
                current++;
            }
        } catch (RemoteException | InterruptedException e) {
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

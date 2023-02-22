import java.rmi.RemoteException;

public class ClientWorker extends Thread {
    private final int start;
    private final int end;
    private final IPrimesService svc;
    private final ICallback stub;

    public ClientWorker(int start, int end, IPrimesService svc, ICallback stub) {
        this.start = start;
        this.end = end;
        this.svc = svc;
        this.stub = stub;

        System.out.println("Novo trabalhador. Inicio " +
                this.start + ". Fim " + this.end);
    }

    @Override
    public void run() {
        try {
            svc.findPrimes(start, end, stub);
        } catch (RemoteException e) {
            System.out.println("Remote exception");
            e.printStackTrace();
        } catch (Exception ex) {
            System.err.println("Client unhandled exception: " + ex.toString());
        }
    }
}

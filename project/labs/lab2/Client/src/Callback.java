import java.io.Serializable;
import java.rmi.RemoteException;

public class Callback implements ICallback, Serializable {
    @Override
    public void nextPrime(int prime) throws RemoteException {
        System.out.println("Numero: " + prime);

    }
}

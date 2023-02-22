import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IPrimesService  extends Remote {
    void findPrimes(int startNumber,
                    int endNumber,
                    ICallback callback) throws RemoteException;
}

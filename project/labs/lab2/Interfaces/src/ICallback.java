import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ICallback  extends Remote {
    void nextPrime(int prime) throws RemoteException;
}

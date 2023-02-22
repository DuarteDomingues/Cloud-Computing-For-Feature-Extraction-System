import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;
import java.util.Scanner;

public class ServerRMI implements IPrimesService {
    static String serverIP = "localhost";
    static int registerPort = 7000;
    static int svcPort = 7001;
    static ServerRMI svc = null;


    public static void main(String[] args) {
        java.util.Scanner scanner = new java.util.Scanner(System.in);

        if (args.length >= 3) {
            serverIP = args[0];
            registerPort = Integer.parseInt(args[1]);
            svcPort = Integer.parseInt(args[2]);
        } else {
            System.out.println("Erro nos argumentos, fornece como argumento o IP, a porta registo e a porta do svc");
            System.out.println("A recorrer aos argumentos por defeito");
        }
        try {
            System.out.println("Servidor aberto no endereço " + serverIP + " registado na porta " + registerPort + " e svc port " + svcPort);
            System.setProperty("java.rmi.server.hostname",serverIP);
            Properties props = System.getProperties();
            props.put("java.rmi.server.hostname", serverIP);

            svc = new ServerRMI();
            IPrimesService stubSvc=(IPrimesService)UnicastRemoteObject.exportObject(svc, svcPort);
            Registry registry = LocateRegistry.createRegistry(registerPort);
            registry.rebind("RemoteServer", stubSvc); //regista skeleton com nome lógico

            System.out.println("Server ready: Press any key to finish server");
            String line = scanner.nextLine();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            System.err.println("Server unhandled exception: " + ex.toString());
        }finally {
            scanner.close();
            System.exit(0);
        }
    }

    @Override
    public void findPrimes(int startNumber, int endNumber, ICallback callback) throws RemoteException {
        try {
            System.out.println("Chamou findPrimes");
            PrimeWorker worker = new PrimeWorker(startNumber, endNumber, callback);
            worker.start();
            System.out.println("A calcular");
            worker.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
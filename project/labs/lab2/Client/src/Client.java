import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class Client {


    private int min = 0;
    private int max = 100;

    private String serverIP = "localhost";
    private int registerPort = 7000;
    private int callPort = 8000;
    private int numThreads = 1;

    public static void main(String[] args) {
        Client client = new Client();

        if (args.length > 0) {
            client.serverIP = args[0];
            if (args.length > 2) {
                client.registerPort = Integer.parseInt(args[1]);
                client.callPort = Integer.parseInt(args[2]);
                if (args.length > 5) {
                    client.min = Integer.parseInt(args[3]);
                    client.max = Integer.parseInt(args[4]);
                    if (args.length > 5) {
                        client.numThreads = Integer.parseInt(args[5]);
                    }
                }
            }

        } else {
            System.out.println("Erro nos argumentos, fornece como argumento o IP, as portas (registo e SVC), os parametros minimo e maximo e o numero de threads");
            System.out.println("A recorrer aos argumentos por defeito");
        }

        try {
            System.out.println("Cliente a tentar conetar ao endereço " + client.serverIP + " registado na porta " + client.registerPort + " e call port " + client.callPort);

            Callback callback = new Callback();
            ICallback stub = (ICallback) UnicastRemoteObject.exportObject(callback, client.callPort);
            Registry registry = LocateRegistry.getRegistry(client.serverIP, client.registerPort);
            IPrimesService svc = (IPrimesService) registry.lookup("RemoteServer");


            int step = (client.max - client.min) / client.numThreads;
            ClientWorker[] workers = new ClientWorker[client.numThreads];
            for (int i = 0; i < client.numThreads; i++) {
                int start = i * step;
                int end = (i + 1) * step;

                ClientWorker worker = new ClientWorker(start, end, svc, stub);
                workers[i] = worker;
            }
            for (int i = 0; i < workers.length; i++) {
                workers[i].start();
            }
            for (int i = 0; i < workers.length; i++) {
                workers[i].join();
            }

            System.exit(0);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }
}

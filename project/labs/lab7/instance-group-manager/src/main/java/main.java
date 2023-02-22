import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstanceGroupManager;
import com.google.cloud.compute.v1.InstanceGroupManagersClient;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.stub.InstancesStub;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class main {
    private static final String DEFAULT_PROJECT = "robust-seat-309417";
    private static final String DEFAULT_ZONE = "europe-west2-c";

    public static void main(String[] args) throws IOException {
        //Usar só a env com a service account correta
        boolean done = false;
        do {
            int option = showOptions();
            switch (option) {
                case 1:
                    listVMInstances(DEFAULT_PROJECT, DEFAULT_ZONE);
                    break;
                case 2:
                    listManagedInstanceGroups(DEFAULT_PROJECT, DEFAULT_ZONE);
                    break;
                case 3:
                    boolean valid = false;
                    Scanner scanner = new Scanner(System.in);
                    int out = 0;
                    do {
                        System.out.println("Quantas VM's?");
                        if (scanner.hasNextInt()) {
                            out = scanner.nextInt();
                            valid = true;
                        } else {
                            System.out.println("Input invalido");
                            scanner.next();
                            continue;
                        }
                    } while (!valid);
                    resizeManagedInstanceGroup(DEFAULT_PROJECT, DEFAULT_ZONE, "instance-group-1", out);
                    break;
                case 99:
                    done = true;
                default:
                    break;
            }
        } while (!done);

    }

    private static int showOptions() {
        String template = "Write %d for %s";
        System.out.println(String.format(template, 1, "listVMInstances"));
        System.out.println(String.format(template, 2, "listManagedInstanceGroups"));
        System.out.println(String.format(template, 3, "resizeManagedInstanceGroup"));
        System.out.println(String.format(template, 99, "exit"));

        Scanner scanner = new Scanner(System.in);
        return scanner.nextInt();
    }


    static void listVMInstances(String project, String zone) throws IOException {
        try (InstancesClient client = InstancesClient.create()) {
            for (Instance e : client.list(project, zone).iterateAll()) {
                if (e.getStatus() == Instance.Status.RUNNING) {
                    String ip = e.getNetworkInterfaces(0).getAccessConfigs(0).getNatIP();
                    System.out.println("Name: " + e.getName() + "; IP: " + ip);
                }
            }
        }
    }

    static void listManagedInstanceGroups(String project, String zone) throws IOException {
        try (InstanceGroupManagersClient client = InstanceGroupManagersClient.create()) {
            for (InstanceGroupManager manager : client.list(project, zone).iterateAll()) {
                System.out.println("Name: " + manager.getName());
                System.out.println("Template: " + manager.getInstanceTemplate());
                System.out.println("Current Size:" + manager.getTargetSize());
            }
        }
    }

    static void resizeManagedInstanceGroup(String project, String zone, String grpName, int newSize)
            throws IOException {
        InstanceGroupManagersClient managersClient = InstanceGroupManagersClient.create();
        managersClient.resize(
                project,
                zone,
                grpName,
                newSize
        );
    }
}

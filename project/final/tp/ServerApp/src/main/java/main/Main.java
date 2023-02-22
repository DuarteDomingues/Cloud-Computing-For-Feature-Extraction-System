package main;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import firestore.FirestoreManipulator;
import grpc.ServiceImpl;
import io.grpc.ServerBuilder;
import org.apache.commons.cli.*;

import java.io.FileInputStream;
import java.io.IOException;

public class Main {
    private static int svcPort = 8000;

    private static GoogleCredentials credentials;
    private static Storage storage;

    public static void main(String[] args) {
        try {
            parseArgs(args);
            FirestoreManipulator firestoreManipulator = new FirestoreManipulator(credentials);

            var server = ServerBuilder.forPort(svcPort).addService(new ServiceImpl(storage, firestoreManipulator)).build();
            server.start();

            server.awaitTermination();
            server.shutdown();
            firestoreManipulator.firestoreClose();

        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    // example : java CommonsCliPgm -f Vikram -l Aruchamy -e name@email.com -m 123456789
    // por defeito uma option não é required
    private static void parseArgs(String[] args) throws IOException {
        Options options = new Options();
        Option svcPortOption = new Option("p", "port", true, "[int] Port where to accept requests");
        Option credentialsOption = new Option("f", "credential", true, "[String] JSON file with credentials for GRPC project");
        options.addOption(svcPortOption);
        options.addOption(credentialsOption);

        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("Select any combo of args", options);
            System.exit(1);
            return;
        }

        if (cmd.hasOption("port")) {
            svcPort = Integer.parseInt(cmd.getOptionValue("port"));
        }
        var defaultCredentials = true;
        if (cmd.hasOption("credential")) {
            credentials = GoogleCredentials.fromStream(new FileInputStream(cmd.getOptionValue("credential")));
            var storageOptions = StorageOptions.newBuilder().setCredentials(credentials).build();
            storage = storageOptions.getService();
            defaultCredentials = false;
        } else {
            credentials = GoogleCredentials.getApplicationDefault();
            var storageOptions = StorageOptions.getDefaultInstance();
            storage = storageOptions.getService();
        }


        System.out.println("Program initialized with following args");
        System.out.printf("Port       : %d\n", svcPort);
        System.out.printf("Credentials: %s\n", (defaultCredentials ? "Default Credentials" : cmd.getOptionValue("credential")));
    }
}

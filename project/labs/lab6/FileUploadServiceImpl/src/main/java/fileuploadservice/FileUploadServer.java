package fileuploadservice;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

public class FileUploadServer extends FileUploadServiceGrpc.FileUploadServiceImplBase {
    private static int svcPort = 80;
    private static Storage storage;
    private static String credentialLocation = "firestore-very-private-key.json";

    public static void main(String[] args) {
        try {
            StorageOptions storageOptions;
            System.out.println("Args len" + args.length);
            if (args.length == 0) {
                storageOptions = StorageOptions.getDefaultInstance();
            } else if (args.length == 1) {
                svcPort = Integer.parseInt(args[0]);
                storageOptions = StorageOptions.getDefaultInstance();
            } else if (args.length == 2) {
                svcPort = Integer.parseInt(args[0]);
                System.out.println("File " + args[1]);
                GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
                storageOptions = StorageOptions.newBuilder().setCredentials(credentials).build();
            } else {
                svcPort = Integer.parseInt(args[0]);
                System.out.println("File   " + args[1]);
                GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(args[1]));
                storageOptions = StorageOptions.newBuilder().setCredentials(credentials).build();
                credentialLocation = args[2];
                System.out.println("File 2 " + args[2]);
            }

            io.grpc.Server svc = ServerBuilder.forPort(svcPort).addService(new FileUploadServer()).build();
            svc.start();
            System.out.println("Server iniciado on port " + svcPort);

            storage = storageOptions.getService();

            Scanner scan = new Scanner((System.in));
            scan.nextLine();
            svc.shutdown();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public StreamObserver<Contents> sendFileBlocks(StreamObserver<Result> responseObserver) {
        try {
            return new ContentStreamObserver(storage, responseObserver, credentialLocation);
        } catch (IOException exception) {
            exception.printStackTrace();
            responseObserver.onError(exception);
            return null;
        }
    }
}

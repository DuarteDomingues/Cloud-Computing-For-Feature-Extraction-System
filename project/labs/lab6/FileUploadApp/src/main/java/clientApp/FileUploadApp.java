package clientApp;

import com.google.protobuf.ByteString;
import fileuploadservice.Contents;
import fileuploadservice.FileUploadServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

public class FileUploadApp {
    private static String svcIP = "localhost";
    //private static String svcIP = "35.246.73.129";
    private static int svcPort = 80;
    private static ManagedChannel channel;
    private static FileUploadServiceGrpc.FileUploadServiceStub noBlockStub;
    private static FileUploadServiceGrpc.FileUploadServiceBlockingStub blockingStub;

    public static void main(String[] args) {
        try {
            if (args.length == 2) {
                svcIP = args[0];
                svcPort = Integer.parseInt(args[1]);
            }
            channel = ManagedChannelBuilder.forAddress(svcIP, svcPort)
                    // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                    // needing certificates.
                    .usePlaintext()
                    .build();
            noBlockStub = FileUploadServiceGrpc.newStub(channel);
            blockingStub = FileUploadServiceGrpc.newBlockingStub(channel);

            Scanner scan = new Scanner(System.in);
            System.out.println("Qual o ficheiro a enviar (Path Absoluto)");
            String filePath = scan.nextLine();
            System.out.println("Qual o nome que o ficheiro deve ter");
            String fileName = scan.nextLine();

            Path uploadFrom = Paths.get(filePath);
            String contentType = Files.probeContentType(uploadFrom);

            System.out.println("A enviar");
            ClientStreamObserver rpyStreamObs = new ClientStreamObserver();
            StreamObserver<Contents> reqs = noBlockStub.sendFileBlocks(rpyStreamObs);

            byte[] buffer = new byte[10240];
            try (InputStream input = Files.newInputStream(uploadFrom)) {
                int limit;
                while ((limit = input.read(buffer)) >= 0) {
                    try {
                        var contents = Contents.newBuilder()
                                .setContentType(contentType)
                                .setFileBlockBytes(ByteString.copyFrom(buffer, 0, limit))
                                .setFilename(fileName)
                                .build();
                        reqs.onNext(contents);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        reqs.onError(ex);
                    }
                }
                reqs.onCompleted();
            }

            while (!rpyStreamObs.isCompleted()) {
                System.out.println("A fazer upload, pls hold");
                Thread.sleep(1000);
            }
            if (rpyStreamObs.OnSuccesss()) {
                System.out.println("Done sending file");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

package App;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import tpFinalService.Content;
import tpFinalService.tpServiceGrpc;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class FileUploadApp {


    private static final String testPath = "C:\\ISEL\\OneDrive - Instituto Superior de Engenharia de Lisboa\\Semestre 6"
            + "\\CN\\tp\\enums\\final\\TrabalhoFinalPublic\\WorkloadImages\\data-center-computer.jpeg";
    //private static String svcIP = "35.188.44.155";
    private static String svcIP = "35.246.75.171"; //35.246.75.171
    private static int svcPort = 8000;


    private static tpServiceGrpc.tpServiceStub noBlockStub;
    private static String instanceGroup = null;
    private final Scanner scan;
    private final CollectionReference collection;
    private final Firestore db;
    private static String instanceName = "instance-group-server";

    public static void main(String[] args) {
        try {
            if (args.length == 2) {
                instanceGroup = args[0];
                svcIP = getIpAddresses(instanceName);
                System.out.println(svcIP);
                svcPort = Integer.parseInt(args[1]);
            }


            //if (args.length == 2) {
            //    svcIP = args[0];
          //      svcPort = Integer.parseInt(args[1]);
          //  }
            if (svcIP != null) {
                System.out.println("Connecting to IP " + svcIP);
            } else {
                System.out.println("Didn't get a valid IP, terminating");
                System.exit(1);
            }
            var app = new FileUploadApp();

            var done = false;
            do {


                var option = app.showMenu();

                switch (option) {
                    case 1:
                        try {
                            var filePath = app.getFilePathToUploadFrom(testPath);
                            var targetName = app.getTargetNameForFile(filePath.getFileName().toString());
                            var succ = app.sendFileInChunks(GlobalVars.CHUNK_SIZE, filePath, targetName);
                            if (succ) {
                                System.out.println("Ficheiro enviado com sucesso");
                            }
                        } catch (NoSuchFileException | InvalidPathException | NullPointerException exception) {
                            System.out.println("O ficheiro não foi encontrado no seu disco, tente de novo");
                        }

                        break;
                    case 2:
                        app.getImageMetadataFromCloud();
                        break;
                    case 3:
                        app.getAllDocumentsInCollection();
                        break;
                    case 4:
                        app.getImageLabelsFromCloud();
                        break;
                    case 5:
                        app.searchImagesByTitle();
                        break;
                    case 6:
                        app.searchImagesByLabel();
                        break;
                    case 99:
                        done = true;
                        break;
                    default:
                        break;
                }

            } while (!done);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    public FileUploadApp() throws IOException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(svcIP, svcPort).usePlaintext().build();
        noBlockStub = tpServiceGrpc.newStub(channel);
        scan = new Scanner(System.in);

        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
        FirestoreOptions options = FirestoreOptions.newBuilder().setCredentials(credentials).build();
        this.db = options.getService();
        this.collection = db.collection(GlobalVars.DEFAULT_COLLECTION);
    }

    public int showMenu() {
        var tag = "=".repeat(10);
        var title = "App de deteção de labels";
        System.out.printf("%s   %s   %s%n%n", tag, title, tag);
        System.out.println();
        System.out.printf("Opcoes %s%n", tag.repeat(3));
        var optionsText = "\tOpção %2d:%5s%10s%n";
        System.out.printf(optionsText, 1, " ", "Carregar um ficheiro para o sistema");
        System.out.printf(optionsText, 2, " ", "Obter metadata de um ficheiro já carregado");
        System.out.printf(optionsText, 3, " ", "Listar imagens disponiveis");
        System.out.printf(optionsText, 4, " ", "Obter labels em portugues de uma imagem");
        System.out.printf(optionsText, 5, " ", "Pesquisar por nome de ficheiro (em desenvolvimento)");
        System.out.printf(optionsText, 6, " ", "Pesquisar imagens com label");
        System.out.printf(optionsText, 99, " ", "Terminar o programa");

        try {
            return Integer.parseInt(scan.nextLine());
        } catch (NumberFormatException ex) {
            System.out.println("Numero inserido não reconhecido");
            return -1;
        }
    }

    public Path getFilePathToUploadFrom(String defaultPath) {
        System.out.println("Qual o ficheiro a enviar (Path Absoluto)");
        String filePath = scan.nextLine();
        if (filePath.equals("")) filePath = defaultPath;
        return Paths.get(filePath);
    }

    public String getTargetNameForFile(String defaultName) {
        System.out.println("Qual o nome para o ficheiro?");
        String targetName = scan.nextLine();
        if (targetName.equals("")) targetName = defaultName;
        return targetName;
    }

    private void getImageMetadataFromCloud() throws ExecutionException, InterruptedException {
        System.out.println("Qual o id da imagem para pesquisar?");
        String targetID = scan.nextLine();

        if (targetID.isBlank()) {
            System.out.println("Can't get image with no ID");
            return;
        }
        getImageMetadataFromCloud(targetID);
    }

    private void getImageMetadataFromCloud(String imageIdentifier) throws ExecutionException, InterruptedException {
        DocumentReference docRef = collection.document(imageIdentifier);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        // future.get() blocks on response
        DocumentSnapshot document = future.get();
        getImageMetadataFromCloud(document);
    }

    private void getImageMetadataFromCloud(DocumentSnapshot document) throws ExecutionException, InterruptedException {
        if (document.exists()) {
            System.out.println("Metadata: " + document.getData());
        } else {
            System.out.printf("Documento com ID:\"%s\" não foi encontrado!%n", document.getId());
        }

    }

    private void getImageLabelsFromCloud() throws ExecutionException, InterruptedException {
        System.out.println("Qual o id da imagem para pesquisar?");
        String targetID = scan.nextLine();

        if (targetID.isBlank()) {
            System.out.println("Can't get image with no ID");
            return;
        }
        getImageLabelsFromCloud(targetID);
    }

    private void getImageLabelsFromCloud(String imageIdentifier) throws ExecutionException, InterruptedException {
        DocumentReference docRef = collection.document(imageIdentifier);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        // future.get() blocks on response
        DocumentSnapshot document = future.get();
        getImageLabelsFromCloud(document);
    }

    private void getImageLabelsFromCloud(DocumentSnapshot document) throws ExecutionException, InterruptedException {
        if (document.exists()) {
            var labels = (List<String>) document.get("labels-pt");
            if (labels != null || labels.size() == 0) {
                System.out.printf("Labels obtidos para a imagem com id %s%n", document.getId());
                for (String label : labels) {
                    System.out.printf("\t%s%n", label);
                }
            } else {
                System.out.println("Não foram encontrados labels para a imagem");
            }
        } else {
            System.out.printf("Documento com ID:\"%s\" não foi encontrado!%n", document.getId());
        }
    }

    private void getAllDocumentsInCollection() throws ExecutionException, InterruptedException {
        //asynchronously retrieve all documents
        ApiFuture<QuerySnapshot> future = collection.get();
        // future.get() blocks on response
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        for (QueryDocumentSnapshot document : documents) {
            getImageMetadataFromCloud(document);
        }
    }

    private void searchImagesByTitle() throws ExecutionException, InterruptedException {
        System.out.println("Qual o nome da imagem?");
        var nome = scan.nextLine();
        searchImagesByTitle(nome);
    }

    private void searchImagesByTitle(String filename) throws ExecutionException, InterruptedException {
        //https://firebase.google.com/docs/firestore/solutions/search - por causa da pesquisa por nome
        // este é um hack super ranhoso, se calhar apagavamos
        ApiFuture<QuerySnapshot> future =
                collection.orderBy("filename").startAt(filename).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        for (DocumentSnapshot document : documents) {
            System.out.println(document.getData());
        }
    }

    private void searchImagesByLabel() throws ExecutionException, InterruptedException {
        System.out.println("Quais os labels da imagem para pesquisar?" +
                "(Se mais que 1, separar por virgulas)");
        String targetLabel = scan.nextLine().toLowerCase();

        if (targetLabel.isBlank()) {
            System.out.println("Não consigo pesquisar se não for fornecido pelo menos um label");
            return;
        }
        searchImagesByLabel(targetLabel);
    }

    private void searchImagesByLabel(String targetLabels) throws ExecutionException, InterruptedException {
        var labels = targetLabels.split(",");

        var query = collection.whereArrayContainsAny("labels-pt", Arrays.asList(labels)).get();
        var docs = query.get().getDocuments();
        for (var doc : docs) {
            getImageMetadataFromCloud(doc);
        }
    }

    public boolean sendFileInChunks(int chunkSize, Path uploadFrom, String targetName) throws IOException {
        String contentType = Files.probeContentType(uploadFrom);
        if (!contentType.matches("image\\/.*")) {
            System.out.println("Apenas é possivel enviar imagens, content type é " + contentType);
            return false;
        }
        System.out.println("A enviar");
        ClientStreamObserver rpyStreamObs = new ClientStreamObserver();
        StreamObserver<Content> reqs = noBlockStub.sendFileBlocks(rpyStreamObs);

        byte[] buffer = new byte[chunkSize];
        try (InputStream input = Files.newInputStream(uploadFrom)) {
            int limit;
            while ((limit = input.read(buffer)) >= 0) {
                try {
                    var contents = Content.newBuilder()
                            .setContentType(contentType)
                            .setFileBlockBytes(ByteString.copyFrom(buffer, 0, limit))
                            .setFilename(targetName)
                            .build();
                    reqs.onNext(contents);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    reqs.onError(ex);
                }
            }
            reqs.onCompleted();

            while (!rpyStreamObs.isCompleted()) {
                System.out.println("A fazer upload, pls hold");
                Thread.sleep(1000);
            }
            return rpyStreamObs.OnSuccesss();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getIpAddresses(String instanceGroup) {
        String cfURL;
        if (instanceGroup != null)
            cfURL = "https://us-central1-robust-seat-309417.cloudfunctions.net/ipLookUp?server=" + instanceGroup;
        else {
            cfURL = "https://us-central1-robust-seat-309417.cloudfunctions.net/ipLookUp";
        }

        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(cfURL)).GET().build();


        HttpResponse<String> response = null;
        try {
            String[] result = null;
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                result = response.body().split(";");
                System.out.println("IP's" + Arrays.toString(result));
                return randomIpChooser(result);
            } else {
                System.out.println("Error receiving Ip addresses!");
                System.out.println("Response " + response);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String randomIpChooser(String[] ipAddresses) {
        Random rd = new Random();
        if (ipAddresses.length == 0)
            return null;
        return ipAddresses[rd.nextInt(ipAddresses.length)];
    }
}

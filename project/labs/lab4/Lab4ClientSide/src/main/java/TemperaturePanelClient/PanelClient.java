package TemperaturePanelClient;

import TemperaturePanelService.*;
import TemperaturePanelService.Void;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class PanelClient extends TemperaturePanelServiceGrpc.TemperaturePanelServiceImplBase implements StreamObserver<Alert> {
    public static void main(String[] args) {
        try {
            PanelClient client;
            if (args.length == 3) {
                client = new PanelClient(args[0], Integer.parseInt(args[1]), args[2]);
            } else {
                System.out.println("No arguments provided, going to defaults");
                client = new PanelClient();
            }

        } catch (
                Exception ex) {
            ex.printStackTrace();
        }

    }

    private boolean isCompleted = false;
    private boolean success = false;

    private TemperatureSensor temperature;

    private PanelID panelID;
    private String local = "Cona country";

    private final TemperaturePanelServiceGrpc.TemperaturePanelServiceBlockingStub blockingStub;
    private final TemperaturePanelServiceGrpc.TemperaturePanelServiceStub noBlockStub;


    public PanelClient() {
        this("localhost", 80, "Arg");
    }

    public PanelClient(String ipAddress, int svcPort, String msg) {
        //create channel
        ManagedChannel channel = ManagedChannelBuilder.forAddress(ipAddress, svcPort).usePlaintext().build();

        //waits for server to respond
        blockingStub = TemperaturePanelServiceGrpc.newBlockingStub(channel);
        //doesnt wait for server to respond
        noBlockStub = TemperaturePanelServiceGrpc.newStub(channel);

        var temperatureService = Local.newBuilder()
                .setMsg(msg)
                .build();
        var idPanel = blockingStub.init(temperatureService);
        if (idPanel != null) {
            this.panelID = idPanel;
        }


        // Regista a instancia atual de Painel como recetor dos alerts.
        // Fica á espera da resposta porque pode haver coisas importantes
        noBlockStub.registerForAlerts(panelID, this);

        blockingStub.sendAlert(Alert.newBuilder()
                .setText(msg).setPanelLocation(this.local)
                .build());
    }

    @Override
    public void onNext(Alert value) {
        System.out.printf("On next called no %s\n\t%s", value.getPanelLocation(),value.getText());
    }

    @Override
    public void onError(Throwable t) {
        this.success = false;
        this.isCompleted = true;
    }

    @Override
    public void onCompleted() {
        this.success = true;
        this.isCompleted = true;
    }

    @Override
    public StreamObserver<Temp> sendTemperatures(StreamObserver<Void> responseObserver) {
        return new TemperatureSensor(this.panelID, responseObserver);
    }

    @Override
    public void sendAlert(Alert alert, StreamObserver<Void> observer) {
        System.err.printf("Houve um alert do painel %d localizado em %s" +
                        "\n\t%s", alert.getPanelID().getId(),
                alert.getPanelLocation(), alert.getText());
    }

    public boolean getIsCompleted() {
        return this.isCompleted;
    }

    public boolean getSuccess() {
        return this.success;
    }
}

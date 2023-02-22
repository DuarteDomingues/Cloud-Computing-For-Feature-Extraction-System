package TemperaturePanelServer;

import TemperaturePanelService.Alert;
import TemperaturePanelService.Local;
import TemperaturePanelService.PanelID;
import TemperaturePanelService.TemperaturePanelServiceGrpc;
import TemperaturePanelService.Void;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.HashMap;

public class AlertHandler implements StreamObserver<Alert> {
    private static String ipAddress = "localhost";
    private static int svcPort = 80;
    private HashMap<PanelID, TemperaturePanelServiceGrpc.TemperaturePanelServiceStub> panelToReceiveAlert;

    public AlertHandler() {
        panelToReceiveAlert = new HashMap<>();
    }

    @Override
    public void onNext(Alert alert) {

    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onCompleted() {
        System.out.println("Completed");
    }

    public void registerForAlerts(PanelID panelID, StreamObserver<Alert> responseObserver) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(ipAddress, svcPort).usePlaintext().build();
        var stub = TemperaturePanelServiceGrpc.newStub(channel);
        panelToReceiveAlert.put(panelID, stub);
        Alert alert = Alert.newBuilder().
                setPanelID(panelID).
                setPanelLocation(Local.newBuilder().setMsg("SERVER-CENTRAL").build()).
                setText("REGISTADO").
                build();

        responseObserver.onNext(alert);
        responseObserver.onCompleted();
    }

    public void sendAlert(Alert alert, StreamObserver<Void> responseObserver) {
        System.out.printf("Server recebeu um alert de %s:\n\t%s%n", alert.getPanelLocation(), alert.getText() );
        for (PanelID panel : panelToReceiveAlert.keySet()) {
            if(panel.getId() != alert.getPanelID().getId()){
                panelToReceiveAlert.get(panel).sendAlert(alert, null);
            }
        }
    }
}

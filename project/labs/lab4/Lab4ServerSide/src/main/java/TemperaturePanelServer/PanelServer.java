package TemperaturePanelServer;

import TemperaturePanelService.*;
import TemperaturePanelService.Void;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class PanelServer extends TemperaturePanelServiceGrpc.TemperaturePanelServiceImplBase implements StreamObserver<Alert> {
    private static String ipAddress = "localhost";
    private static int svcPort = 80;

    private HashMap<PanelID, TemperaturePanelServiceGrpc.TemperaturePanelServiceStub> panelToReceiveAlert;
    private int count;

    private ArrayList<Alert> alerts;

    public PanelServer() {
        count = 0;
        panelToReceiveAlert = new HashMap<>();
        alerts = new ArrayList<>();
    }


    public static void main(String[] args) {
        try {
            if (args.length == 1) {
                svcPort = Integer.parseInt(args[0]);
            }
            io.grpc.Server svc = ServerBuilder.forPort(svcPort).addService(new PanelServer()).build();
            svc.start();
            System.out.println("Server iniciado");
            Scanner scan = new Scanner((System.in));
            scan.nextLine();
            svc.shutdown();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void init(Local request, StreamObserver<PanelID> responseObserver) {
        String panelLocal = request.getMsg();
        var idPanel = PanelID.newBuilder().
                setId(count++)
                .build();

        responseObserver.onNext(idPanel);
        responseObserver.onCompleted();

    }

    @Override
    public void getAllTemperatures(PanelID request, StreamObserver<AllTemps> responseObserver) {
        super.getAllTemperatures(request, responseObserver);


    }

    @Override
    public void registerForAlerts(PanelID panelID, StreamObserver<Alert> responseObserver) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(ipAddress, svcPort).usePlaintext().build();
        var stub = TemperaturePanelServiceGrpc.newStub(channel);
        panelToReceiveAlert.put(panelID, stub);
        Alert alert = Alert.newBuilder().
                setPanelID(panelID).
                setPanelLocation("SERVER-CENTRAL").
                setText("REGISTADO").
                build();

        responseObserver.onNext(alert);
        responseObserver.onCompleted();
    }

    @Override
    public void sendAlert(Alert alert, StreamObserver<Void> responseObserver) {
        System.out.printf("Server recebeu um alert de %s:\n\t%s%n", alert.getPanelLocation(), alert.getText() );
        for (PanelID panel : panelToReceiveAlert.keySet()) {
            if(panel.getId() != alert.getPanelID().getId()){
                panelToReceiveAlert.get(panel).sendAlert(alert, null);
            }
        }
    }


    @Override
    public void onNext(Alert alert) {

    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onCompleted() {

    }
}

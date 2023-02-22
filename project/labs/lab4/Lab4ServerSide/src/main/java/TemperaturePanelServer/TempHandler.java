package TemperaturePanelServer;

import TemperaturePanelService.Alert;
import TemperaturePanelService.Temp;
import io.grpc.stub.StreamObserver;

public class TempHandler implements StreamObserver<Temp> {
    @Override
    public void onNext(Temp temp) {
        System.out.printf("Received Temp on server %s from %s", temp.getTemperature(), temp.getIdentifier());
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Erro");
    }

    @Override
    public void onCompleted() {
        System.out.println("Done");
    }
}

package TemperaturePanelClient;

import TemperaturePanelService.PanelID;
import TemperaturePanelService.Temp;
import TemperaturePanelService.Void;
import io.grpc.stub.StreamObserver;

import java.util.Random;

public class TemperatureSensor implements StreamObserver<Temp> {

    private double tempValue = 20;
    private PanelID panelId;
    private StreamObserver<Void> sReplies;
    private Random r;


    public TemperatureSensor(PanelID panelID, StreamObserver<Void> sReplies) {
        this.panelId = panelID;
        this.sReplies = sReplies;
        this.r = new Random();

    }


    public double getTemperature() {
        this.tempValue = (15 + (25 - 15) * r.nextDouble());
        return this.tempValue;
    }


    public Temp getTemp() {
        var tempSend = Temp.newBuilder().
                setTemperature(getTemperature())
                .setIdentifier(this.panelId)
                .build();

        return tempSend;

    }

    @Override
    public void onNext(Temp temp) {

    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onCompleted() {

    }
}

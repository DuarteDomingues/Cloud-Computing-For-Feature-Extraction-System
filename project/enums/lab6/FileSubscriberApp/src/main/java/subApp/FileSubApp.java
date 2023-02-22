package subApp;

import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;

import java.util.Scanner;

public class FileSubApp {
    private static String PROJECT_ID = "robust-seat-309417";
    private static String subsName = "cn-g14-sub";

    public static void main(String[] args) {
        ProjectSubscriptionName projsubscriptionName =
                ProjectSubscriptionName.of(PROJECT_ID, subsName);
        Subscriber subscriber =
                Subscriber.newBuilder(projsubscriptionName,
                        new MessageReceiveHandler())
                        .build();
        subscriber.startAsync().awaitRunning();

        System.out.println("Waiting for messages. \n(Press any key to shutdown)");
        String s = (new Scanner(System.in)).nextLine();
        subscriber.stopAsync();
    }
}
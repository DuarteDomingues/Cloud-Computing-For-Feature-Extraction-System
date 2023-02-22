package vision_worker;

import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.pubsub.v1.ProjectSubscriptionName;

import java.io.IOException;

public class VisionWorkerMain {


    public static void main(String[] args) {

        try {
            System.out.println("Creating Topic Admin");
            TopicAdminClient topicAdmin = TopicAdminClient.create();
            System.out.println("Creating Subscription");
            ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(GlobalVars.DEFAULT_PROJECT, GlobalVars.DEFAULT_SUBSCRIPTION);
            //gets messages that are received in pub-sub

            System.out.println("Creating Subscriber");
            Subscriber subscriber = Subscriber.newBuilder(subscriptionName, new MessageRecieverHandler()).build();

            subscriber.startAsync().awaitRunning();
            System.out.println("Vision Worker is running");

            subscriber.awaitTerminated();
            subscriber.stopAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
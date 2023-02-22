package subApp;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.pubsub.v1.PubsubMessage;

public class MessageReceiveHandler implements MessageReceiver {
    public void receiveMessage(PubsubMessage msg, AckReplyConsumer ackReply) {
        System.out.println("Message (Id:" + msg.getMessageId() +
                " Data:" + msg.getData().toStringUtf8() + ")");
        ackReply.ack();
    }
}

package com.Elessar.app.server;

import com.Elessar.app.client.HttpClient;
import com.Elessar.database.MyDatabase;
import com.Elessar.proto.Sendmessage.SendMsgRequest;
import com.Elessar.proto.Sendmessage.SendMsgResponse;
import com.google.api.client.http.HttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;

/**
 * Created by Hans on 1/29/19.
 */
public class MsgPushSender implements MsgSender {
    private static final Logger logger = LogManager.getLogger(MsgPushSender.class);
    private final MyDatabase db;
    public MsgPushSender (MyDatabase db) {
        this.db = db;
    }

    @Override
    public void send(Message message, String URL, HttpClient httpClient) {
        try {
            final SendMsgRequest.Builder sendMsgRequest = SendMsgRequest.newBuilder();
            sendMsgRequest.setFromUser(message.getFromUser())
                          .setToUser(message.getToUser())
                          .setText(message.getText())
                          .setTimestamp(message.getTimestamp());
            final HttpResponse response = httpClient.post(new URL(URL + "/sendMessage"), sendMsgRequest.build());
            SendMsgResponse sendMsgResponse = SendMsgResponse.parseFrom(response.getContent());
            if (sendMsgResponse.getSuccess()) {
                logger.info("Message successfully sent from {} to {}", message.getFromUser(), message.getToUser());
                Message readMsg = new Message(message.getFromUser(),
                                              message.getToUser(),
                                              message.getText(),
                                              message.getTimestamp(),
                                              true);
                db.findAndUpdate(new Message(message.getFromUser(), message.getToUser(), message.getText(), message.getTimestamp(), false),
                                 new Message(message.getFromUser(), message.getToUser(), message.getText(), message.getTimestamp(), true));
            } else {
                logger.info("Message fail to send to {} because {}", message.getToUser(), sendMsgResponse.getFailReason());
            }
        } catch (Exception e) {
            logger.error("Caught exception during sending message from {} to {}: {}", message.getFromUser(), message.getToUser(), e.getMessage());
        }
    }

}

package com.Elessar.app.server;

import com.Elessar.app.client.HttpClient;
import com.Elessar.database.MyDatabase;
import com.Elessar.proto.P2Pmessage.P2PMsgRequest;
import com.Elessar.proto.P2Pmessage.P2PMsgResponse;
import com.google.api.client.http.HttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.net.URL;

/**
 * Implemented in push model
 * Created by Hans on 1/29/19.
 */
public class DirectMsgSender implements MsgSender {
    private static final Logger logger = LogManager.getLogger(DirectMsgSender.class);
    private final MyDatabase db;
    public DirectMsgSender(MyDatabase db) {
        this.db = db;
    }

    @Override
    public P2PMsgResponse send(Message message, String URL, HttpClient httpClient) {
        P2PMsgResponse p2pMsgResponse = null;
        try {
            final P2PMsgRequest.Builder p2pMsgRequest = P2PMsgRequest.newBuilder();
            p2pMsgRequest.setFromUser(message.getFromUser())
                          .setToUser(message.getToUser())
                          .setText(message.getText())
                          .setTimestamp(message.getTimestamp());
            final HttpResponse response = httpClient.post(new URL(URL + "/p2pMessage"), p2pMsgRequest.build());
            p2pMsgResponse = P2PMsgResponse.parseFrom(response.getContent());

        } catch (Exception e) {
            logger.error("Caught exception during sending message from {} to {}: {}", message.getFromUser(), message.getToUser(), e.getMessage());
        }

        return p2pMsgResponse;
    }

}

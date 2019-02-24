package com.Elessar.app.server;

import com.Elessar.app.util.HttpClient;
import com.Elessar.proto.P2Pmsg;
import com.Elessar.proto.P2Pmsg.P2PMsgRequest;
import com.Elessar.proto.P2Pmsg.P2PMsgResponse;
import com.google.api.client.http.HttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.net.URL;
import java.util.List;

/**
 * Implemented in push model
 * Created by Hans on 1/29/19.
 */
public class DirectMsgSender implements MsgSender {
    private static final Logger logger = LogManager.getLogger(DirectMsgSender.class);
    private final HttpClient httpClient;

    public DirectMsgSender(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public P2PMsgResponse send(List<Message> messages, String URL) throws Exception {
        final P2PMsgRequest.Builder p2pMsgRequest = P2PMsgRequest.newBuilder();
        String fromUser = messages.get(0).getFromUser();
        String toUser = messages.get(0).getToUser();

        for (Message message : messages) {
            p2pMsgRequest.setFromUser(fromUser)
                         .setToUser(toUser)
                         .addMessage(P2Pmsg.Message.newBuilder()
                                 .setText(message.getText())
                                 .setTimestamp(message.getTimestamp()));
        }

        final HttpResponse response = httpClient.post(new URL(URL + "/p2pMessage"), p2pMsgRequest.build());
        return P2PMsgResponse.parseFrom(response.getContent());
    }

}

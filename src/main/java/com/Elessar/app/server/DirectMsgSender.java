package com.Elessar.app.server;

import com.Elessar.app.util.HttpClient;
import com.Elessar.app.util.Metric;
import com.Elessar.app.util.MetricManager;
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
    private final MetricManager metricManager;
    private final HttpClient httpClient;

    public DirectMsgSender(HttpClient httpClient, MetricManager metricManager) {
        this.httpClient = httpClient;
        this.metricManager = metricManager;
    }

    @Override
    public P2PMsgResponse send(List<Message> messages, String URL) throws Exception {
        final Metric metric = metricManager.newMetric(new StringBuilder().append(MsgSender.MSG_SENDER).append(".")
                                                                         .append(MsgSender.SEND).toString());

        final P2PMsgRequest.Builder p2pMsgRequest = P2PMsgRequest.newBuilder();
        final String fromUser = messages.get(0).getFromUser();
        final String toUser = messages.get(0).getToUser();

        p2pMsgRequest.setFromUser(fromUser).setToUser(toUser);

        for (Message message : messages) {
            p2pMsgRequest.addMessage(P2Pmsg.Message.newBuilder()
                                 .setText(message.getText())
                                 .setTimestamp(message.getTimestamp()));
        }

        final HttpResponse response = httpClient.post(new URL(URL + "/p2pMessage"), p2pMsgRequest.build());
        final P2PMsgResponse p2PMsgResponse = P2PMsgResponse.parseFrom(response.getContent());

        metric.timerStop();

        return p2PMsgResponse;
    }

    @Override
    public void close() {};

}

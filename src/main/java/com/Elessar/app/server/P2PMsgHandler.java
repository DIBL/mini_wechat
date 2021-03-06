package com.Elessar.app.server;

import com.Elessar.app.util.Metric;
import com.Elessar.app.util.MetricManager;
import com.Elessar.database.MyDatabase;
import com.Elessar.proto.P2Pmsg;
import com.Elessar.proto.P2Pmsg.P2PMsgRequest;
import com.Elessar.proto.P2Pmsg.P2PMsgResponse;
import com.google.common.cache.LoadingCache;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hans on 1/27/19.
 */
public class P2PMsgHandler implements HttpHandler {
    private static final Logger logger = LogManager.getLogger(P2PMsgHandler.class);
    private final MyDatabase db;
    private final MsgSender msgSender;
    private final LoadingCache<String, User> users;
    private final MetricManager metricManager;
    private final String mode;

    public P2PMsgHandler(MyDatabase db, MsgSender msgSender, LoadingCache<String, User> users, String mode, MetricManager metricManager) {
        this.db = db;
        this.msgSender = msgSender;
        this.users = users;
        this.mode = mode;
        this.metricManager = metricManager;
    }

    @Override
    public void handle(HttpExchange he) throws IOException {
        final Metric metric = metricManager.newMetric(new StringBuilder().append(MyServer.SERVER).append(".")
                                                                           .append(MyServer.P2P_MSG).toString());

        final String requestType = he.getRequestMethod();
        // Only handle POST request
        if (!"POST".equals(requestType)) {
            logger.debug("Received non-POST request for person to person messaging");
            final String response = "NOT IMPLEMENTED\n";
            he.sendResponseHeaders(501, response.length());  // 501 tells the caller that this method is not supported by the server
            try (OutputStream os = he.getResponseBody()) {
                os.write(response.getBytes());
            }
            return;
        }

        try (final InputStream is = he.getRequestBody()) {
            final P2PMsgRequest p2pMsgRequest = P2PMsgRequest.parseFrom(is);
            final P2PMsgResponse.Builder p2pMsgResponse = P2PMsgResponse.newBuilder();
            final String fromUser = p2pMsgRequest.getFromUser();
            final String toUser = p2pMsgRequest.getToUser();
            final User existingUser = users.getUnchecked(toUser);

            if (existingUser.getName() == null) {
                // remove dummy entry as this is an invalid request
                users.invalidate(toUser);

                logger.info("Can NOT send message to {} because {} is NOT registered !", toUser, toUser);
                p2pMsgResponse.setSuccess(false)
                              .setIsDelivered(false)
                              .setFailReason(toUser + " is NOT registered");
                he.sendResponseHeaders(400, 0);
            } else {

                List<Message> messages = new ArrayList<>();
                for (P2Pmsg.Message msg : p2pMsgRequest.getMessageList()) {
                    messages.add(new Message(fromUser, toUser, msg.getText(), System.currentTimeMillis(), false)); // Sync msg to server's current time
                }

                try {
                    db.insert(messages);
                    logger.debug("Messages stored in database successfully");

                    if (existingUser.getOnline()) {
                        if ("pull".equals(mode)) {
                            p2pMsgResponse.mergeFrom(msgSender.send(messages, existingUser.getName()));
                        } else {
                            p2pMsgResponse.mergeFrom(msgSender.send(messages, existingUser.getURL()));
                        }

                        if (p2pMsgResponse.getSuccess() && p2pMsgResponse.getIsDelivered()) {
                            logger.debug("Message successfully sent from {} to {}", fromUser, toUser);
                            db.update(messages, new Message(null, null, null, null, true));
                            he.sendResponseHeaders(200, 0);

                        } else {
                            logger.error("Message fail to send to {} because {}", toUser, p2pMsgResponse.getFailReason());
                            he.sendResponseHeaders(500, 0);
                        }

                    } else {
                        p2pMsgResponse.setSuccess(true)
                                      .setIsDelivered(false);
                        he.sendResponseHeaders(200, 0);
                    }

                } catch (Exception e) {
                    logger.error("Caught exception during send message: {}", e.getMessage());
                    p2pMsgResponse.setSuccess(false)
                                  .setIsDelivered(false)
                                  .setFailReason(e.getMessage());
                    he.sendResponseHeaders(500, 0);
                }
            }

            try (final OutputStream os = he.getResponseBody()) {
                p2pMsgResponse.build().writeTo(os);
            }
        }

        metric.timerStop();
    }
}

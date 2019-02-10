package com.Elessar.app.server;

import com.Elessar.app.client.HttpClient;
import com.Elessar.database.MyDatabase;
import com.Elessar.proto.P2Pmessage.P2PMsgRequest;
import com.Elessar.proto.P2Pmessage.P2PMsgResponse;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by Hans on 1/27/19.
 */
public class SendMsgHandler implements HttpHandler {
    private static final Logger logger = LogManager.getLogger(SendMsgHandler.class);
    private final MyDatabase db;
    private final HttpClient httpClient;
    private final MsgSender msgSender;
    public SendMsgHandler(MyDatabase db, HttpClient httpClient) {
        this.db = db;
        this.httpClient = httpClient;
        this.msgSender = new DirectMsgSender(db);
    }

    @Override
    public void handle(HttpExchange he) throws IOException {
        final String requestType = he.getRequestMethod();
        // Only handle POST request
        if (!"POST".equals(requestType)) {
            logger.debug("Received non-POST request for log on");
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
            final String toUser = p2pMsgRequest.getToUser();
            final Message message = new Message(p2pMsgRequest.getFromUser(),
                                                p2pMsgRequest.getToUser(),
                                                p2pMsgRequest.getText(),
                                                System.currentTimeMillis(), false);     // Sync msg to server's current time
            List<User> receivers = db.find(new User(toUser, null, null, null, null, null));
            if (receivers.isEmpty()) {
                logger.info("Can NOT send message to {} because {} is NOT registered !", toUser, toUser);
                p2pMsgResponse.setSuccess(false).setFailReason(toUser + " is NOT registered");
                he.sendResponseHeaders(400, 0);
            } else {
                User receiver = receivers.get(0);
                try {
                    db.insert(message);
                    logger.debug("Message stored in database successfully");
                    if (receiver.getOnline()) {
                        p2pMsgResponse.mergeFrom(msgSender.send(message, receiver.getURL(), httpClient));
                        if (p2pMsgResponse.getSuccess() && p2pMsgResponse.getIsDelivered()) {
                            logger.debug("Message successfully sent from {} to {}", message.getFromUser(), message.getToUser());
                            db.findAndUpdate(new Message(message.getFromUser(), message.getToUser(), message.getText(), message.getTimestamp(), false),
                                             new Message(message.getFromUser(), message.getToUser(), message.getText(), message.getTimestamp(), true));
                            he.sendResponseHeaders(200, 0);
                        } else {
                            logger.info("Message fail to send to {} because {}", message.getToUser(), p2pMsgResponse.getFailReason());
                            he.sendResponseHeaders(500, 0);
                        }
                    } else {
                        p2pMsgResponse.setSuccess(true);
                        p2pMsgResponse.setIsDelivered(false);
                        he.sendResponseHeaders(200, 0);
                    }
                } catch (Exception e) {
                    logger.error("Caught exception during storing message into database: {}", e.getMessage());
                    p2pMsgResponse.setSuccess(false).setFailReason(e.getMessage());
                    he.sendResponseHeaders(500, 0);
                }
            }

            try (final OutputStream os = he.getResponseBody()) {
                p2pMsgResponse.build().writeTo(os);
            }
        }
    }
}

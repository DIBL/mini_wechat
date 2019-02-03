package com.Elessar.app.server;

import com.Elessar.app.client.HttpClient;
import com.Elessar.database.MyDatabase;
import com.Elessar.proto.Sendmessage.SendMsgRequest;
import com.Elessar.proto.Sendmessage.SendMsgResponse;
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
        this.msgSender = new MsgPushSender(db);
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
            final SendMsgRequest sendMsgRequest = SendMsgRequest.parseFrom(is);
            final SendMsgResponse.Builder sendMsgResponse = SendMsgResponse.newBuilder();
            final String toUser = sendMsgRequest.getToUser();
            final Message message = new Message(sendMsgRequest.getFromUser(),
                                                sendMsgRequest.getToUser(),
                                                sendMsgRequest.getText(),
                                                sendMsgRequest.getTimestamp(), false);
            List<User> receivers = db.find(new User(toUser, null, null, null, null, null));
            if (receivers.isEmpty()) {
                logger.info("Can NOT send message to {} because {} is NOT registered !", toUser, toUser);
                sendMsgResponse.setSuccess(false).setFailReason(toUser + " is NOT registered");
                he.sendResponseHeaders(400, 0);
            } else {
                User receiver = receivers.get(0);
                try {
                    db.insert(message);
                    logger.info("Message stored in database successfully");
                    sendMsgResponse.setSuccess(true);
                    he.sendResponseHeaders(200, 0);
                    if (receiver.getOnline()) {
                        msgSender.send(message, receiver.getURL(), httpClient);
                    }
                } catch (Exception e) {
                    logger.error("Caught exception during storing message into database: {}", e.getMessage());
                    sendMsgResponse.setSuccess(false).setFailReason(e.getMessage());
                    he.sendResponseHeaders(500, 0);     // should use 40X to 50X ??
                }
            }

            try (final OutputStream os = he.getResponseBody()) {
                sendMsgResponse.build().writeTo(os);
            }
        }
    }
}

package com.Elessar.app.server;

import com.Elessar.database.MyDatabase;
import com.Elessar.proto.Logon.LogonResponse;
import com.Elessar.proto.Logon.LogonRequest;
import com.Elessar.proto.P2Pmsg.P2PMsgResponse;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


/**
 * Created by Hans on 1/16/19.
 */
public class LogOnHandler implements HttpHandler {
    private static final Logger logger = LogManager.getLogger(LogOnHandler.class);
    private final MyDatabase db;
    private final MsgSender msgSender;
    public LogOnHandler(MyDatabase db, MsgSender msgSender) {
        this.db = db;
        this.msgSender = msgSender;
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
            return ;
        }

        try (final InputStream is = he.getRequestBody()) {
            final LogonRequest logonRequest = LogonRequest.parseFrom(is);
            final LogonResponse.Builder logonResponse = LogonResponse.newBuilder();
            final String userName = logonRequest.getName();
            final String url = he.getRemoteAddress().toString();

            final User prevUser = db.update(new User(userName, logonRequest.getPassword(), null, null, url, true));

            if (prevUser == null) {
                logger.info("User {} and password combination does NOT exist !", userName);
                logonResponse.setSuccess(false).setFailReason("User " + userName + " password combination does NOT exist !");
                he.sendResponseHeaders(400, 0);
            } else if (prevUser.getOnline()) {
                logger.info("User {} has already log on !", userName);
                logonResponse.setSuccess(true);
                he.sendResponseHeaders(200, 0);
            } else {
                logger.info("User {} successfully log on !", userName);
                logonResponse.setSuccess(true);
                he.sendResponseHeaders(200, 0); //2nd arg = 0 means chunked encoding is used, an arbitrary number of bytes may be written

                // Get the list of unread messages sent to user
                List<Message> messages = db.find(new Message(null, userName, null, null, false));

                try {
                    P2PMsgResponse p2pMsgResponse = msgSender.send(messages, url);

                    if (p2pMsgResponse.getSuccess() && p2pMsgResponse.getIsDelivered()) {
                        db.update(new Message(null, userName, null, null, false),
                                  new Message(null, null, null, null, true));
                    } else {
                        logger.info("Fail to send unread messages to {} during log on because {}", userName, p2pMsgResponse.getFailReason());
                    }

                } catch (Exception e) {
                    logger.error("Caught exception when sending messages during log on: {}", e.getMessage());
                }

            }

            try (final OutputStream os = he.getResponseBody()) {
                logonResponse.build().writeTo(os);
            }
        }
    }

}

package com.Elessar.app.server;

import com.Elessar.database.MyDatabase;
import com.Elessar.proto.Logon.LogonResponse;
import com.Elessar.proto.Logon.LogonRequest;
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
    public LogOnHandler(MyDatabase db) {
        this.db = db;
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

            final User prevUser = db.update(new User(userName,
                                                     logonRequest.getPassword(), null, null,
                                                     logonRequest.getClientURL(), true));
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
                // Get the list of unread messages sent to user and update them to read
                List<Message> messages = db.findAndUpdate(new Message(null, userName, null, null, false),
                                                          new Message(null, userName, null, null, true));
                for (Message message : messages) {
                    logonResponse.addMessages(
                            Logon.UnreadMsg.newBuilder()
                            .setFromUser(message.getFromUser())
                            .setToUser(message.getToUser())
                            .setText(message.getText())
                            .setTimestamp(message.getTimestamp()));
                }
                logonResponse.setSuccess(true);
                he.sendResponseHeaders(200, 0); //2nd arg = 0 means chunked encoding is used, an arbitrary number of bytes may be written
            }

            try (final OutputStream os = he.getResponseBody()) {
                logonResponse.build().writeTo(os);
            }
        }
    }

}

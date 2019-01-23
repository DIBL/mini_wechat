package com.Elessar.app.server;

import com.Elessar.database.MyDatabase;
import com.Elessar.proto.Logoff.LogoffResponse;
import com.Elessar.proto.Logoff.LogoffRequest;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Created by Hans on 1/16/19.
 */
public class LogOffHandler implements HttpHandler {
    private static final Logger logger = LogManager.getLogger(LogOffHandler.class);
    private final MyDatabase db;

    public LogOffHandler(MyDatabase db) {
        this.db = db;
    }

    @Override
    public void handle(HttpExchange he) throws IOException {
        final String requestType = he.getRequestMethod();
        // Only handle POST request
        if (!"POST".equals(requestType)) {
            logger.debug("Received non-POST request for log off");
            final String response = "NOT IMPLEMENTED\n";
            he.sendResponseHeaders(501, response.length());  // 501 tells the caller that this method is not supported by the server
            try (OutputStream os = he.getResponseBody()) {
                os.write(response.getBytes());
            }
            return ;
        }

        try (final InputStream is = he.getRequestBody()) {
            final LogoffRequest logoffRequest = LogoffRequest.parseFrom(is);
            final LogoffResponse.Builder logoffResponse = LogoffResponse.newBuilder();
            final String userName = logoffRequest.getName();
            User prevUser = db.update(new User(userName, null, null, null, "false"));
            if (prevUser == null) {
                logger.info("User {} is NOT registered !", userName);
                logoffResponse.setSuccess(false).setFailReason("User " + userName + " is NOT a registered !");
                he.sendResponseHeaders(400, 0);
            } else if (prevUser.getOnline().equals("false")) {
                logger.info("User {} has already log off !", userName);
                logoffResponse.setSuccess(false).setFailReason("User " + userName + " has already log off !");
                he.sendResponseHeaders(400, 0);
            } else {
                logger.info("User {} successfully log off !", userName);
                logoffResponse.setSuccess(true);
                he.sendResponseHeaders(200, 0);
            }

            try (final OutputStream os = he.getResponseBody()) {
                logoffResponse.build().writeTo(os);
            }
        }
    }

}

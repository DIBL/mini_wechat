package com.Elessar.app.server;

import com.Elessar.database.MyDatabase;
import com.Elessar.proto.Logon.LogonResponse;
import com.Elessar.proto.Logon.LogonRequest;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Created by Hans on 1/16/19.
 */
public class LogOnHandler implements HttpHandler {
    private static final Logger logger = LogManager.getLogger(LogOnHandler.class);
    private final MyDatabase users;
    public LogOnHandler(MyDatabase users) {
        this.users = users;
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
            final Map<String, String> filters = new HashMap<>();
            filters.put("name", logonRequest.getName());
            filters.put("password", logonRequest.getPassword());

            Iterator<Document> cursor = users.find(filters).iterator();
            if (!cursor.hasNext()) {
                logger.info("User {} and password combination does NOT exist !", userName);
                logonResponse.setSuccess(false).setFailReason("User " + userName + " password combination does NOT exist !");
                he.sendResponseHeaders(400, 0);
            } else {
                Document doc = cursor.next();
                if (users.isFieldEqual(doc, "online", "true")) {
                    logger.info("User {} has already log on !", userName);
                    logonResponse.setSuccess(false).setFailReason("User " + userName + " has already log on !");
                    he.sendResponseHeaders(400, 0);
                } else {
                    logger.info("User {} successfully log on !", userName);
                    users.updateField(filters, "online", "true");
                    logonResponse.setSuccess(true);
                    he.sendResponseHeaders(200, 0);
                }
            }

            try (final OutputStream os = he.getResponseBody()) {
                logonResponse.build().writeTo(os);
            }
        }
    }

}

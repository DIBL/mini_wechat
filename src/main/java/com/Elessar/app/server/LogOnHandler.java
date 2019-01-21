package com.Elessar.app.server;

import com.Elessar.proto.Logon.LogonResponse;
import com.Elessar.proto.Logon.LogonRequest;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Updates.set;

/**
 * Created by Hans on 1/16/19.
 */
public class LogOnHandler implements HttpHandler {
    private static final Logger logger = LogManager.getLogger(LogOnHandler.class);
    private final MongoCollection<Document> user;

    public LogOnHandler(MongoCollection<Document> user) {
        this.user = user;
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
            final String password = logonRequest.getPassword();
            Bson query = and(eq("name", userName), eq("password", password));
            try (MongoCursor<Document> cursor = user.find(query).projection(fields(include("online"), excludeId())).iterator()) {
                if (!cursor.hasNext()) {
                    logger.info("User {} and password combination does NOT exist !", userName);
                    logonResponse.setSuccess(false).setFailReason("User " + userName + " password combination does NOT exist !");
                    he.sendResponseHeaders(400, 0);
                } else if (cursor.next().getBoolean("online")){
                    logger.info("User {} has already log on !", userName);
                    logonResponse.setSuccess(false).setFailReason("User " + userName + " has already log on !");
                    he.sendResponseHeaders(400, 0);
                } else {
                    logger.info("User {} successfully log on !", userName);
                    user.updateOne(query, set("online", true));
                    logonResponse.setSuccess(true);
                    he.sendResponseHeaders(200, 0);
                }
            }

            try (final OutputStream os = he.getResponseBody()){
                logonResponse.build().writeTo(os);
            }
        }
    }

}

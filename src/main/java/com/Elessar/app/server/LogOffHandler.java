package com.Elessar.app.server;

import com.Elessar.proto.Logoff.LogoffResponse;
import com.Elessar.proto.Logoff.LogoffRequest;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Created by Hans on 1/16/19.
 */
public class LogOffHandler implements HttpHandler {
    private final Map<String, User> userData;

    public LogOffHandler(Map<String, User> userData) {
        this.userData = userData;
    }

    @Override
    public void handle(HttpExchange he) throws IOException {
        final String requestType = he.getRequestMethod();
        // Only handle POST request
        if (!"POST".equals(requestType)) {
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
            if (!userData.containsKey(userName)) {
                logoffResponse.setSuccess(false).setFailReason(userName + " is NOT a Registered User !");
                he.sendResponseHeaders(400, 0);
            } else if (!userData.get(userName).getOnlineStatus()){
                logoffResponse.setSuccess(false).setFailReason(userName + " is already log off !");
                he.sendResponseHeaders(400, 0);
            } else {
                userData.get(userName).setOffline();
                logoffResponse.setSuccess(true);
                he.sendResponseHeaders(200, 0);
            }

            try (final OutputStream os = he.getResponseBody()){
                logoffResponse.build().writeTo(os);
            }
        }
    }

}

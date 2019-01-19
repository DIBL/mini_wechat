package com.Elessar.app.server;

import com.Elessar.proto.Logon.LogonResponse;
import com.Elessar.proto.Logon.LogonRequest;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Created by Hans on 1/16/19.
 */
public class LogOnHandler implements HttpHandler {
    private final Map<String, User> userData;

    public LogOnHandler(Map<String, User> userData) {
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
            final LogonRequest logonRequest = LogonRequest.parseFrom(is);
            final LogonResponse.Builder logonResponse = LogonResponse.newBuilder();
            final String userName = logonRequest.getName();
            final String password = logonRequest.getPassword();
            if (!userData.containsKey(userName)) {
                logonResponse.setSuccess(false).setFailReason("User Name " + userName + " is NOT Registered !");
                he.sendResponseHeaders(400, 0);
            } else if (!userData.get(userName).getPassword().equals(password)){
                //System.out.printf("user = %s, password1 = %s, password2 = %s\n", userName, userData.get(userName).getPassword(), password);
                logonResponse.setSuccess(false).setFailReason("Incorrect Password !");
                he.sendResponseHeaders(400, 0);
            } else if (userData.get(userName).getOnlineStatus()) {
                logonResponse.setSuccess(false).setFailReason(userName + " is already log on !");
                he.sendResponseHeaders(400, 0);
            } else {
                userData.get(userName).setOnline();
                logonResponse.setSuccess(true);
                he.sendResponseHeaders(200, 0);
            }

            try (final OutputStream os = he.getResponseBody()){
                logonResponse.build().writeTo(os);
            }
        }
    }

}

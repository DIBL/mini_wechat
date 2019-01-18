package com.Elessar.app.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.Elessar.proto.Registration.RegistrationResponse;
import com.Elessar.proto.Registration.RegistrationRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Created by Hans on 1/15/19.
 */
public class RegisterHandler implements HttpHandler {
    private final Map<String, User> userData;

    public RegisterHandler(Map<String, User> userData) {
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
            final RegistrationRequest regRequest = RegistrationRequest.parseFrom(is);
            final RegistrationResponse.Builder regResponse = RegistrationResponse.newBuilder();

            if (userData.containsKey(regRequest.getName())) {
                regResponse.setSuccess(false).setFailReason("User Name " + regRequest.getName() + " Exists !");
                he.sendResponseHeaders(400, 0);
            } else {
                userData.put(regRequest.getName(), new User(regRequest.getName(), regRequest.getPassword(), regRequest.getEmail(), regRequest.getPhoneNumber()));
                regResponse.setSuccess(true);
                he.sendResponseHeaders(200, 0);
            }

            try (final OutputStream os = he.getResponseBody()){
                regResponse.build().writeTo(os);
            }
        }
    }
}

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
    private final Map<String, RegistrationRequest> userData;

    public RegisterHandler(Map<String, RegistrationRequest> userData) {
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

        try (InputStream is = he.getRequestBody()) {
            RegistrationRequest regRequest = RegistrationRequest.parseFrom(is);
            if (userData.containsKey(regRequest.getName())) {
                final RegistrationResponse.Builder regResponse = RegistrationResponse.newBuilder();
                regResponse.setSuccess(false);
                regResponse.setFailReason("User Name Exists !");
                he.sendResponseHeaders(200, 0);
                try (OutputStream os = he.getResponseBody()){
                    regResponse.build().writeTo(os);
                }
            } else {
                userData.put(regRequest.getName(), regRequest);
                final RegistrationResponse.Builder regResponse = RegistrationResponse.newBuilder();
                regResponse.setSuccess(true);
                he.sendResponseHeaders(200, 0);
                try (OutputStream os = he.getResponseBody()){
                    regResponse.build().writeTo(os);
                }
            }
        }
    }
}

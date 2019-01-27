package com.Elessar.app.server;

import com.Elessar.database.MyDatabase;
import com.Elessar.database.OperationsMonitor;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.Elessar.proto.Registration.RegistrationResponse;
import com.Elessar.proto.Registration.RegistrationRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Created by Hans on 1/15/19.
 */
public class RegisterHandler implements HttpHandler {
    private static final Logger logger = LogManager.getLogger(RegisterHandler.class);
    private final MyDatabase db;
    private final OperationsMonitor monitor;

    public RegisterHandler(MyDatabase db, OperationsMonitor monitor) {
        this.db = db;
        this.monitor = monitor;
    }

    @Override
    public void handle(HttpExchange he) throws IOException {
        final String requestType = he.getRequestMethod();
        // Only handle POST request
        if (!"POST".equals(requestType)) {
            logger.debug("Received non-POST request for registration");
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
            final String userName = regRequest.getName();
            try {
                monitor.timerStart(MyDatabase.INSERT);
                db.insert(new User(regRequest.getName(),
                        regRequest.getPassword(),
                        regRequest.getEmail(),
                        regRequest.getPhoneNumber(), false));
                monitor.timerEnd(MyDatabase.INSERT);
                logger.info("User {} successfully registered !", userName);
                regResponse.setSuccess(true);
                he.sendResponseHeaders(200, 0);
            } catch (Exception e) {
                logger.error("User name {} already exists, caught exception: {}", userName, e.getMessage());
                regResponse.setSuccess(false).setFailReason("User Name " + userName + " Exists !");
                he.sendResponseHeaders(400, 0);
            }

            try (final OutputStream os = he.getResponseBody()){
                regResponse.build().writeTo(os);
            }
        }
        monitor.timerReset(MyDatabase.INSERT);
    }
}

package com.Elessar.app.server;

import com.Elessar.app.util.Metric;
import com.Elessar.app.util.MetricManager;
import com.Elessar.database.MyDatabase;
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
    private final MetricManager metricManager;

    public RegisterHandler(MyDatabase db, MetricManager metricManager) {
        this.db = db;
        this.metricManager = metricManager;
    }

    @Override
    public void handle(HttpExchange he) throws IOException {
        final Metric metric = new Metric(metricManager, new StringBuilder().append(MyServer.SERVER).append(".")
                                                                           .append(MyServer.REGISTER).toString());

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

        try {
            metric.timerStart();
        } catch (Exception e) {
            logger.debug("Caught exception when trying to start timer during handle register request: {}", e.getMessage());
        }

        try (final InputStream is = he.getRequestBody()) {
            final RegistrationRequest regRequest = RegistrationRequest.parseFrom(is);
            final RegistrationResponse.Builder regResponse = RegistrationResponse.newBuilder();
            final String userName = regRequest.getName();
            try {
                db.insert(new User(regRequest.getName(),
                                   regRequest.getPassword(),
                                   regRequest.getEmail(),
                                   regRequest.getPhoneNumber(),
                                   "",
                                   false));
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

        try {
            metric.timerStop();
        } catch (Exception e) {
            logger.debug("Caught exception when trying to stop timer during handle register request: {}", e.getMessage());
        }
    }
}

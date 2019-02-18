package com.Elessar.app.server;

import com.Elessar.app.util.Metric;
import com.Elessar.app.util.MetricManager;
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
    private final MetricManager metricManager;

    public LogOffHandler(MyDatabase db, MetricManager metricManager) {
        this.db = db;
        this.metricManager = metricManager;
    }

    @Override
    public void handle(HttpExchange he) throws IOException {
        final Metric metric  = new Metric(metricManager, new StringBuilder().append(MyServer.SERVER).append(".")
                                                                            .append(MyServer.LOGOFF).toString());

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

        try {
            metric.timerStart();
        } catch (Exception e) {
            logger.debug("Caught exception when trying to start timer during handle log off request: {}", e.getMessage());
        }

        try (final InputStream is = he.getRequestBody()) {
            final LogoffRequest logoffRequest = LogoffRequest.parseFrom(is);
            final LogoffResponse.Builder logoffResponse = LogoffResponse.newBuilder();
            final String userName = logoffRequest.getName();

            final User prevUser = db.update(new User(userName, null, null, null, null, false));
            if (prevUser == null) {
                logger.info("User {} is NOT registered !", userName);
                logoffResponse.setSuccess(false).setFailReason("User " + userName + " is NOT a registered !");
                he.sendResponseHeaders(400, 0);
            } else if (!prevUser.getOnline()) {
                logger.info("User {} has already log off !", userName);
                logoffResponse.setSuccess(true);
                he.sendResponseHeaders(200, 0);
            } else {
                logger.info("User {} successfully log off !", userName);
                logoffResponse.setSuccess(true);
                he.sendResponseHeaders(200, 0);
            }

            try (final OutputStream os = he.getResponseBody()) {
                logoffResponse.build().writeTo(os);
            }
        }

        try {
            metric.timerStop();
        } catch (Exception e) {
            logger.debug("Caught exception when trying to stop timer during handle log off request: {}", e.getMessage());
        }
    }

}

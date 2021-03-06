package com.Elessar.app.server;

import com.Elessar.app.util.Metric;
import com.Elessar.app.util.MetricManager;
import com.Elessar.database.MyDatabase;
import com.Elessar.proto.Logon.LogonResponse;
import com.Elessar.proto.Logon.LogonRequest;
import com.Elessar.proto.P2Pmsg.P2PMsgResponse;
import com.google.common.cache.LoadingCache;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


/**
 * Created by Hans on 1/16/19.
 */
public class LogOnHandler implements HttpHandler {
    private static final Logger logger = LogManager.getLogger(LogOnHandler.class);
    private final MyDatabase db;
    private final MsgSender msgSender;
    private final LoadingCache<String, User> users;
    private final MetricManager metricManager;
    private final String mode;


    public LogOnHandler(MyDatabase db, MsgSender msgSender, LoadingCache<String, User> users, String mode, MetricManager metricManager) {
        this.db = db;
        this.msgSender = msgSender;
        this.users = users;
        this.metricManager = metricManager;
        this.mode = mode;
    }

    @Override
    public void handle(HttpExchange he) throws IOException {
        final Metric metric = metricManager.newMetric(new StringBuilder().append(MyServer.SERVER).append(".")
                                                                           .append(MyServer.LOGON).toString());

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
            final String clientURL = new StringBuilder().append("http://")
                                                        .append(he.getRemoteAddress().getAddress().getHostAddress())
                                                        .append(":")
                                                        .append(logonRequest.getPort()).toString();

            final User existingUser = users.getUnchecked(userName);
            final User newUser = new User(userName, password, existingUser.getEmail(), existingUser.getPhoneNumber(), clientURL, true);

            if (existingUser.getName() == null || !password.equals(existingUser.getPassword())) {
                // remove dummy entry as this is an invalid request
                if (existingUser.getName() == null) {
                    users.invalidate(userName);
                }

                logger.info("User {} and password combination does NOT exist !", userName);
                logonResponse.setSuccess(false).setFailReason("User " + userName + " password combination does NOT exist !");
                he.sendResponseHeaders(400, 0);
            } else if (existingUser.getOnline()) {
                logger.info("User {} has already log on !", userName);
                logonResponse.setSuccess(true);
                he.sendResponseHeaders(200, 0);

                if (!clientURL.equals(existingUser.getURL())) {
                    db.update(newUser);
                    users.put(userName, newUser);
                }
            } else {
                logger.info("User {} successfully log on !", userName);
                logonResponse.setSuccess(true);
                he.sendResponseHeaders(200, 0); //2nd arg = 0 means chunked encoding is used, an arbitrary number of bytes may be written

                db.update(newUser);
                users.put(userName, newUser);

                // Get the list of unread messages sent to user
                List<Message> messages = db.find(new Message(null, userName, null, null, false));
                if ("pull".equals(mode)) {
                    sendMessages(messages, userName);
                } else {
                    sendMessages(messages, clientURL);
                }
            }

            try (final OutputStream os = he.getResponseBody()) {
                logonResponse.build().writeTo(os);
            }
        }

        metric.timerStop();
    }

    private void sendMessages(List<Message> messages, String toUser) {
        if (messages.isEmpty()) {
            return;
        }

        try {
            P2PMsgResponse p2pMsgResponse = msgSender.send(messages, toUser);

            if (p2pMsgResponse.getSuccess() && p2pMsgResponse.getIsDelivered()) {
                db.update(messages, new Message(null, null, null, null, true));
            } else {
                logger.info("Fail to retrieve unread messages during log on because {}", p2pMsgResponse.getFailReason());
            }

        } catch (Exception e) {
            logger.error("Caught exception when sending messages during log on: {}", e.getMessage());
        }
    }
}

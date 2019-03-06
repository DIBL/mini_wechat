package com.Elessar.app.client;

import com.Elessar.app.util.Metric;
import com.Elessar.app.util.MetricManager;
import com.Elessar.proto.P2Pmsg.P2PMsgRequest;
import com.Elessar.proto.P2Pmsg.P2PMsgResponse;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;

/**
 * Created by Hans on 2/1/19.
 */
public class MyClientServer {
    private static final String CLIENT_SERVER = "clientServer", P2P_MSG = "p2pMsg";
    private static final Logger logger = LogManager.getLogger(MyClientServer.class);
    private final String serverName;
    private final int port;
    private final BlockingQueue<String> messageQueue;
    private final MetricManager metricManager;

    public MyClientServer(String serverName, int port, BlockingQueue<String> messageQueue, MetricManager metricManager) {
        this.serverName = serverName;
        this.port = port;
        this.messageQueue = messageQueue;
        this.metricManager = metricManager;
    }

    public void run() {
        try {
            final HttpServer server = HttpServer.create(new InetSocketAddress(serverName, port), 0);
            server.createContext("/p2pMessage", new P2PMsgHandler(messageQueue, metricManager));
            server.setExecutor(Executors.newFixedThreadPool(2));
            server.start();
            logger.info("Client Server started at port {}", port);
        } catch (IOException e) {
            logger.fatal("Caught exception during client server startup at port {}: {}", port, e.getMessage());
        }
    }

    private static class P2PMsgHandler implements HttpHandler {
        private MetricManager metricManager;
        private BlockingQueue<String> messageQueue;

        public P2PMsgHandler (BlockingQueue<String> messageQueue, MetricManager metricManager) {
            this.messageQueue = messageQueue;
            this.metricManager = metricManager;
        }

        @Override
        public void handle(HttpExchange he) throws IOException {
            final Metric metric = metricManager.newMetric(new StringBuilder().append(CLIENT_SERVER).append(".")
                                                                               .append(P2P_MSG).toString());

            final String requestType = he.getRequestMethod();
            // Only handle POST request
            if (!"POST".equals(requestType)) {
                logger.debug("Received non-POST request for send message");
                final String response = "NOT IMPLEMENTED\n";
                he.sendResponseHeaders(501, response.length());  // 501 tells the caller that this method is not supported by the server
                try (OutputStream os = he.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }

            try (final InputStream is = he.getRequestBody()) {
                final P2PMsgRequest p2pMsgRequest = P2PMsgRequest.parseFrom(is);
                final P2PMsgResponse.Builder p2pMsgResponse = P2PMsgResponse.newBuilder();

                try {
                    messageQueue.put(p2pMsgRequest.toString());
                    logger.debug("Messages received by {}", p2pMsgRequest.getToUser());

                    p2pMsgResponse.setSuccess(true)
                                  .setIsDelivered(true);
                    he.sendResponseHeaders(200, 0);
                } catch (Exception e) {
                    logger.error("Caught exception during receiving messages {}", e.getMessage());
                    p2pMsgResponse.setSuccess(false)
                                  .setIsDelivered(false)
                                  .setFailReason(e.getMessage());
                    he.sendResponseHeaders(400, 0);
                }

                try (final OutputStream os = he.getResponseBody()) {
                    p2pMsgResponse.build().writeTo(os);
                }
            }

            metric.timerStop();
        }
    }
}

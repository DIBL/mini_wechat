package com.Elessar.app.client;

import com.Elessar.proto.P2Pmessage.P2PMsgRequest;
import com.Elessar.proto.P2Pmessage.P2PMsgResponse;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Queue;

/**
 * Created by Hans on 2/1/19.
 */
public class MyClientServer {
    private static final Logger logger = LogManager.getLogger(MyClientServer.class);
    private final String serverName;
    private final int port;
    private final Queue<String> unreadMsgs;

    public MyClientServer(String serverName, int port, Queue<String> unreadMsgs) {
        this.serverName = serverName;
        this.port = port;
        this.unreadMsgs = unreadMsgs;
    }

    public void run() {
        try {
            final HttpServer server = HttpServer.create(new InetSocketAddress(serverName, port), 0);
            server.createContext("/p2pMessage", new p2pMsgHandler(unreadMsgs));
            server.setExecutor(null);
            server.start();
            logger.info("Client Server started at port {}", port);
        } catch (IOException e) {
            logger.fatal("Caught exception during client server startup at port {}: {}", port, e.getMessage());
        }
    }

    private static class p2pMsgHandler implements HttpHandler {
        private Queue<String> unreadMsgs;

        public p2pMsgHandler (Queue<String> unreadMsgs) {
            this.unreadMsgs = unreadMsgs;
        }

        @Override
        public void handle(HttpExchange he) throws IOException {
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

                unreadMsgs.offer(p2pMsgRequest.toString());

                logger.debug("Message received by {}", p2pMsgRequest.getToUser());
                p2pMsgResponse.setSuccess(true);
                p2pMsgResponse.setIsDelivered(true);
                he.sendResponseHeaders(200, 0);
                try (final OutputStream os = he.getResponseBody()) {
                    p2pMsgResponse.build().writeTo(os);
                }
            }
        }
    }
}

package com.Elessar.app.client;

import com.Elessar.proto.Sendmessage.SendMsgRequest;
import com.Elessar.proto.Sendmessage.SendMsgResponse;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * Created by Hans on 2/1/19.
 */
public class MyClientServer {
    private static final Logger logger = LogManager.getLogger(MyClientServer.class);
    private final String serverName;
    private final int port;

    public MyClientServer(String serverName, int port) {
        this.serverName = serverName;
        this.port = port;
    }

    public void run() {
        try {
            final HttpServer server = HttpServer.create(new InetSocketAddress(serverName, port), 0);
            server.createContext("/sendMessage", new SendMsgHandler());
            server.setExecutor(null);
            server.start();
            logger.info("Client Server started at port {}", port);
        } catch (IOException e) {
            logger.fatal("Caught exception during client server startup: {}", e.getMessage());
        }
    }

    private static class SendMsgHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            final String requestType = he.getRequestMethod();
            // Only handle POST request
            if (!"POST".equals(requestType)) {
                logger.debug("Received non-POST request for log on");
                final String response = "NOT IMPLEMENTED\n";
                he.sendResponseHeaders(501, response.length());  // 501 tells the caller that this method is not supported by the server
                try (OutputStream os = he.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }

            try (final InputStream is = he.getRequestBody()) {
                final SendMsgRequest sendMsgRequest = SendMsgRequest.parseFrom(is);
                final SendMsgResponse.Builder sendMsgResponse = SendMsgResponse.newBuilder();
                System.out.println(sendMsgRequest.toString());
                logger.info("Message successfully sent from {} to {}", sendMsgRequest.getFromUser(), sendMsgRequest.getToUser());
                sendMsgResponse.setSuccess(true);
                he.sendResponseHeaders(200, 0);
                try (final OutputStream os = he.getResponseBody()) {
                    sendMsgResponse.build().writeTo(os);
                }
            }
        }
    }
}

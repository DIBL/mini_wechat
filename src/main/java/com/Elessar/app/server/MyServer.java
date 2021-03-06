package com.Elessar.app.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.concurrent.Executors;

import com.Elessar.app.util.MetricManager;
import com.Elessar.database.MyDatabase;
import com.google.common.cache.LoadingCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Hans on 1/10/19.
 */
@Component
public class MyServer {
    public static final String SERVER = "server", LOGON = "logon", LOGOFF = "logoff", REGISTER = "register", P2P_MSG = "p2pMsg";
    private static final Logger logger = LogManager.getLogger(MyServer.class);
    private final InetSocketAddress serverAddress;
    private final MyDatabase db;
    private final MsgSender msgSender;
    private final MetricManager metricManager;
    private final LoadingCache<String, User> users;
    private HttpServer server;
    private final String mode;

    @Autowired
    public MyServer(String mode, MyDatabase db, LoadingCache<String, User> users, MsgSender msgSender, InetSocketAddress serverAddress, MetricManager metricManager) {
        this.mode = mode;
        this.db = db;
        this.users = users;
        this.msgSender = msgSender;
        this.serverAddress = serverAddress;
        this.metricManager = metricManager;
    }

    public void run() {
        try {
            server = HttpServer.create(serverAddress, 0);
            server.createContext("/", new RootHandler());
            server.createContext("/echo", new EchoHandler());
            server.createContext("/register", new RegisterHandler(db, users, metricManager));
            server.createContext("/logon", new LogOnHandler(db, msgSender, users, mode, metricManager));
            server.createContext("/logoff", new LogOffHandler(db, users, metricManager));
            server.createContext("/p2pMessage", new P2PMsgHandler(db, msgSender, users, mode, metricManager));
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            logger.info("Server started at port {}", serverAddress.getPort());

        } catch (IOException e) {
            logger.fatal("Caught exception during server startup: {}", e.getMessage());
            msgSender.close();
        }
    }

    public void stop() {
        server.stop(0);
    }

    private static class RootHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            final String response = "<h1>Server start success if you see this message</h1>" + "<h1>Port: " + 9000 + "</h1>";
            he.sendResponseHeaders(200, response.length()); // 200 means the request was successfully received
            try (OutputStream os = he.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    private static class EchoHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            final String requestType = he.getRequestMethod();
            // Only process GET request
            if (!"GET".equals(requestType)) {
                logger.trace("Handle non-GET echo request {}", he);
                final String response = "NOT IMPLEMENTED\n";
                // 501 tells the caller that this method is not supported by the server
                he.sendResponseHeaders(501, response.length());
                try (OutputStream os = he.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }

            try (InputStream is = he.getRequestBody()) {
                final Scanner s = new Scanner(is).useDelimiter("\\A");
                String request = s.hasNext() ? s.next() : "";
                logger.info("Handle echo request: {}", request);
            }

            final String response = "OK\n";
            he.sendResponseHeaders(200, response.length());
            try (OutputStream os = he.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}

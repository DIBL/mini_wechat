package com.Elessar.app.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import com.Elessar.database.MyDatabase;
import com.Elessar.database.OperationsMonitor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Created by Hans on 1/10/19.
 */
public class MyServer {
    private static final Logger logger = LogManager.getLogger(MyServer.class);

    private final String serverName;
    private final int port;
    private final MyDatabase db;
    private final OperationsMonitor monitor;
    private static Timer timer;

    public MyServer(String serverName, int port, MyDatabase db) {
        this.serverName = serverName;
        this.port = port;
        this.db = db;
        this.monitor = new OperationsMonitor();
        timer = new Timer();
    }

    public void run() {
        try {
            final HttpServer server = HttpServer.create(new InetSocketAddress(serverName, port), 0);
            server.createContext("/", new RootHandler());
            server.createContext("/echo", new EchoHandler());
            server.createContext("/register", new RegisterHandler(db, monitor));
            server.createContext("/logon", new LogOnHandler(db, monitor));
            server.createContext("/logoff", new LogOffHandler(db, monitor));
            server.setExecutor(null);
            server.start();
            logger.info("Server started at port {}", port);
        } catch (IOException e) {
            logger.fatal("Caught exception during server startup: {}", e);
        }
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (String operation : monitor.getOperationSet()) {
                    logger.printf(Level.INFO, "%s operation takes %.2f ms on average", operation, monitor.getAvgTime(operation));
                    logger.info("{} operation is performed {} times", operation, monitor.getCount(operation));
                }
            }
        }, 10000, 300000);
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
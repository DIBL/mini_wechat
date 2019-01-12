package com.Elessar.app.server;

//import org.apache.commons.io.IOUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * Created by Hans on 1/10/19.
 */
public class MyServer {
    private final String serverName;
    private final int port;

    public MyServer(String serverName, int port) {
        this.serverName = serverName;
        this.port = port;
    }

    public void run() {
        try {
            final HttpServer server = HttpServer.create(new InetSocketAddress(serverName, port), 0);
            server.createContext("/", new RootHandler());
            server.createContext("/echo", new EchoHandler());
            server.setExecutor(null);
            server.start();
            System.out.println("server started at " + port);
        } catch (IOException e) {
            System.out.println("Cannot create server at port!");
        }
    }

    private static class RootHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            final String response = "<h1>Server start success if you see this message</h1>" + "<h1>Port: " + 9000 + "</h1>";
            he.sendResponseHeaders(200, response.length());     // 200 means the request was successfully received
            try (OutputStream os = he.getResponseBody()){
                os.write(response.getBytes());
            }
        }
    }

    private static class EchoHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            final String requestType = he.getRequestMethod();
            // Only process GET request
            if (requestType.equals("POST")) {
                return ;
            }

            try (InputStream is = he.getRequestBody()) {
                //final String request = IOUtils.toString(is, StandardCharsets.UTF_8);
                final Scanner s = new Scanner(is).useDelimiter("\\A");
                String request = s.hasNext() ? s.next() : "";
                System.out.println(request);
            }

            final String response = "OK";
            he.sendResponseHeaders(200, response.length());
            try (OutputStream os = he.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}





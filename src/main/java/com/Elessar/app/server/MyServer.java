package com.Elessar.app.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Hans on 1/10/19.
 */
public class MyServer {
    String serverName;
    int port;

    public MyServer(String serverName, int port) {
        this.serverName = serverName;
        this.port = port;
    }

    public void run() {
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(serverName, port), 0);
        } catch (IOException e) {
            System.out.println("Cannot create server at port!");
        }
        System.out.println("server started at " + port);
        server.createContext("/", new RootHandler());
        server.createContext("/echo", new EchoHandler());
        server.setExecutor(null);
        server.start();
    }
}

class RootHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange he) throws IOException {
        String response = "<h1>Server start success if you see this message</h1>" + "<h1>Port: " + 9000 + "</h1>";
        he.sendResponseHeaders(200, response.length());     // 200 means the request was successfully received
        OutputStream os = he.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}

class EchoHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange he) throws IOException {
        Headers headers = he.getRequestHeaders();
        Set<Map.Entry<String, List<String>>> entries = headers.entrySet();
        String request = "";
        for (Map.Entry<String, List<String>> entry : entries)
            request += entry.toString() + "\n";
        System.out.println(request);

        String response = "OK";
        he.sendResponseHeaders(200, response.length());
        OutputStream os = he.getResponseBody();
        os.write(response.toString().getBytes());
        os.close();
    }
}

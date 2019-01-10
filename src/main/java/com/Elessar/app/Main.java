package com.Elessar.app;
import com.Elessar.app.server.MyServer;

/**
 * Created by Hans on 1/7/19.
 */

public class Main {
    public static void main(String[] args){
        final MyServer server = new MyServer("localhost", 9000);
        server.run();
    }
}



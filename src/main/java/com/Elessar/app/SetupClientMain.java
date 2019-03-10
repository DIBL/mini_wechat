package com.Elessar.app;

import com.Elessar.app.client.MyClientServer;
import com.Elessar.app.util.MetricManager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Hans on 3/6/19.
 */
public class SetupClientMain {
    /**
     *
     * @param args [0] client port number
     */
    public static void main(String[] args){
        final MetricManager metricManager = new MetricManager("ClientMetric", 100);
        final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

        final int clientPort = Integer.valueOf(args[0]);
        final MyClientServer clientServer = new MyClientServer("localhost", clientPort, messageQueue, metricManager);
        clientServer.run();
    }
}

package com.Elessar.app.client;

import com.Elessar.app.util.MetricManager;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by Hans on 3/18/19.
 */
public class BlockingMsgQueue implements MsgQueue {
    private final BlockingQueue<String> queue;
    private final MyClientServer clientServer;

    public BlockingMsgQueue(int clientPort, MetricManager metricManager) {
        this.queue = new LinkedBlockingQueue<>();
        this.clientServer = new MyClientServer("localhost", clientPort, this, metricManager);
        clientServer.run();
    }

    @Override
    public List<String> poll(Duration timeout) throws Exception {
        System.out.println("This is push mode in client");
        final List<String> messageList = new ArrayList<>();
        //BlockingQueue return the head of this queue, or null if the specified waiting time elapses before an element is available
        final String message = queue.poll(timeout.getSeconds(), TimeUnit.SECONDS);

        if (message != null) {
            messageList.add(message);
        }
        return messageList;
    }

    @Override
    public void close() {
        clientServer.stop();
    }

    public void put(String str) throws InterruptedException {
        queue.put(str);
    }
}

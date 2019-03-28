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
        final List<String> messageList = new ArrayList<>();
        messageList.add(queue.poll(timeout.getSeconds(), TimeUnit.SECONDS));
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

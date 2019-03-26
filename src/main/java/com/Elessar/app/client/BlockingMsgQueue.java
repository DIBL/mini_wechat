package com.Elessar.app.client;

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

    public BlockingMsgQueue() {
        this.queue = new LinkedBlockingQueue<>();
    }

    @Override
    public List<String> poll(Duration timeout) throws Exception {
        final List<String> messageList = new ArrayList<>();
        messageList.add(queue.poll(timeout.getSeconds(), TimeUnit.SECONDS));
        return messageList;
    }

    @Override
    public void close() {}

    public void put(String str) throws InterruptedException {
        queue.put(str);
    }
}

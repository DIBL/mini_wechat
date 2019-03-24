package com.Elessar.app.client;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by Hans on 3/18/19.
 */
public class BlockingMsgQueue implements MsgQueue {
    private final BlockingQueue<String> queue;

    public BlockingMsgQueue(BlockingQueue<String> queue) {
        this.queue = queue;
    }

    @Override
    public List<String> poll(Duration timeout) throws Exception {
        final List<String> messageList = new ArrayList<>();
        messageList.add(queue.poll(timeout.getSeconds(), TimeUnit.SECONDS));
        return messageList;
    }

    @Override
    public void close() {}
}

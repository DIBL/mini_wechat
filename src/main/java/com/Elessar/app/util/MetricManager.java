package com.Elessar.app.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Hans on 2/16/19.
 */
public class MetricManager {
    private final Logger logger;
    private final Map<String, Duration> operationDuration;
    private final int maxEntryCount;
    private final Lock lock;


    public MetricManager(String name, int maxEntryCount) {
        logger = LogManager.getLogger(name);
        this.operationDuration = new HashMap<>();
        this.maxEntryCount = maxEntryCount;
        this.lock = new ReentrantLock();
    }

    public void add(String op, Duration duration) {
        lock.lock();
        try {
            operationDuration.put(op, duration);
            if (operationDuration.size() > maxEntryCount) {
                writeToFile();
            }
        } finally {
            lock.unlock();
        }
    }

    private void writeToFile() {
        for (Map.Entry<String, Duration> entry : operationDuration.entrySet()) {
            logger.info("Ignored", entry.getKey(), entry.getValue().getTimestamp(), entry.getValue().getDuration());
        }

        operationDuration.clear();
    }
}

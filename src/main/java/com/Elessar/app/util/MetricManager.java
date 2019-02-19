package com.Elessar.app.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Hans on 2/16/19.
 */
public class MetricManager {
    private final Logger logger;
    private final Set<Metric> metrics;
    private final int maxEntryCount;
    private final Lock lock;


    public MetricManager(String name, int maxEntryCount) {
        logger = LogManager.getLogger(name);
        this.metrics = new HashSet<>();
        this.maxEntryCount = maxEntryCount;
        this.lock = new ReentrantLock();
    }

    public Metric newMetric(String operation) {
        return new Metric(this, operation);
    }

    public void add(Metric m) {
        lock.lock();
        try {
            metrics.add(m);
            if (metrics.size() > maxEntryCount) {
                dumpToFile();
            }
        } finally {
            lock.unlock();
        }
    }

    private void dumpToFile() {
        for (Metric m : metrics) {
            logger.info("Ignored", m.getOperation(), m.getStartTime().toEpochMilli(), m.getDuration().toMillis());
        }

        metrics.clear();
    }
}

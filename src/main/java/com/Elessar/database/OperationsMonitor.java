package com.Elessar.database;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Hans on 1/26/19.
 */
public class OperationsMonitor {
    private final Map<String, MetricMonitor> monitor;
    public OperationsMonitor() {
        monitor = new HashMap<>();
    }

    // in millisecond
    public double getAvgTime(String operation) {
        if (!monitor.containsKey(operation)) {
            monitor.put(operation, new MetricMonitor());
        }
        return monitor.get(operation).getAvgTime();
    }

    public void timerStart(String operation) {
        if (!monitor.containsKey(operation)) {
            monitor.put(operation, new MetricMonitor());
        }
        monitor.get(operation).timerStart();
    }

    // in millisecond
    public double timerEnd(String operation) {
        if (!monitor.containsKey(operation)) {
            monitor.put(operation, new MetricMonitor());
        }
        return monitor.get(operation).timerEnd();
    }

    public void timerReset(String operation) {
        if (!monitor.containsKey(operation)) {
            return ;
        }
        monitor.get(operation).timerReset();
    }

    public int getCount(String operation) {
        if (!monitor.containsKey(operation)) {
            monitor.put(operation, new MetricMonitor());
        }
        return monitor.get(operation).getCount();
    }

    public Set<String> getOperationSet() {
        return monitor.keySet();
    }
}

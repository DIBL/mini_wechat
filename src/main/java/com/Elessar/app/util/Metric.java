package com.Elessar.app.util;

/**
 * Created by Hans on 2/16/19.
 */
public class Metric {
    private final MetricManager metricManager;
    private long startTime;
    private String operation;

    public Metric(MetricManager metricManager, String operation) {
        this.metricManager = metricManager;
        this.operation = operation;
        this.startTime = 0;
    }

    public void timerStart() throws Exception {
        if (startTime != 0) {
            throw new Exception("Timer already starts for this operation");
        }

        startTime = System.currentTimeMillis();
    }

    public void timerStop() throws Exception {
        if (startTime == 0) {
            throw new Exception("Timer has NOT started for this operation");
        }

        long duration = System.currentTimeMillis() - startTime;
        metricManager.add(operation, new Duration(startTime, duration));
        startTime = 0;
    }

}

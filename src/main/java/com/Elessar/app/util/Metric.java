package com.Elessar.app.util;

import java.time.Duration;
import java.time.Instant;

/**
 * Created by Hans on 2/16/19.
 */
public class Metric {
    private final MetricManager metricManager;
    private Instant startTime;
    private Duration duration;
    private String operation;

    public Metric(MetricManager metricManager, String operation) {
        this.metricManager = metricManager;
        this.operation = operation;
        this.startTime = Instant.now();
    }


    public void timerStop() {
        duration = Duration.between(startTime, Instant.now());
        metricManager.add(this);
    }

    public String getOperation() {
        return operation;
    }

    public Duration getDuration() {
        return duration;
    }

    public Instant getStartTime() {
        return startTime;
    }
}

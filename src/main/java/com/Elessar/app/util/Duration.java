package com.Elessar.app.util;

/**
 * Created by Hans on 2/16/19.
 */
public class Duration {
    private long timestamp;
    private long duration;

    public Duration(long timestamp, long duration) {
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getDuration() {
        return duration;
    }
}

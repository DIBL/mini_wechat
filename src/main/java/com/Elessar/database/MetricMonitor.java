package com.Elessar.database;


import com.Elessar.app.server.RegisterHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Hans on 1/26/19.
 */
public class MetricMonitor {
    private static final Logger logger = LogManager.getLogger(MetricMonitor.class);
    private int count;
    private long totalTime;
    private long start;
    public MetricMonitor() {
        this.count = 0;
        this.totalTime = 0;
        this.start = 0;
    }

    /**
     * Get average time
     * @return average time per op in milliseconds
     */
    public double getAvgTime() {
        if (count == 0) {
            return 0;
        }
        return (double) totalTime / count;
    }

    public void timerStart() {
        start = System.currentTimeMillis();
    }

    public double timerEnd() {
        if (start == 0) {
            logger.info("Time has NOT started yet");
            return 0;
        }
        long time = System.currentTimeMillis() - start;
        timerReset();
        totalTime += time;
        count += 1;
        return (double) time;
    }

    public void timerReset() {
        start = 0;
    }

    public int getCount() {
        return count;
    }
}

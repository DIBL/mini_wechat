package com.Elessar.app;

import com.Elessar.app.client.BlockingMsgQueue;
import com.Elessar.app.client.MsgQueue;
import com.Elessar.app.util.MetricManager;

/**
 * Created by Hans on 3/6/19.
 */
public class SetupClientMain {
    /**
     *
     * @param args [0] client port number
     */
    public static void main(String[] args){
        final MetricManager metricManager = new MetricManager("ClientMetric", 100);
        final int clientPort = Integer.valueOf(args[0]);
        final MsgQueue msgQueue = new BlockingMsgQueue(clientPort, metricManager);
    }
}

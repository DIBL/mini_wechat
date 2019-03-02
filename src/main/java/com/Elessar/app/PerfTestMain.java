package com.Elessar.app;

import com.Elessar.app.client.MyClient;
import com.Elessar.app.client.MyClientServer;
import com.Elessar.app.util.MetricManager;
import com.Elessar.proto.P2Pmsg.P2PMsgResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Hans on 2/27/19.
 */
public class PerfTestMain {
    private static final Logger logger = LogManager.getLogger(PerfTestMain.class);

    /**
     *
     * @param args [0] username for user1
     *             [1] port number for user1
     *             [2] username for user2
     *             [3] port number for user2
     *             [4] total number of messages to send
     */
    public static void main(String[] args) throws Exception {
        final String serverURL = new StringBuilder().append("http://127.0.0.1:9000").toString();
        final MetricManager metricManager = new MetricManager("ClientMetric", 100);

        final String username1 = args[0];
        final String password1 = "qwe123052vawqw";
        final String phone1 = getRandomNum(10);
        final BlockingQueue<String> messageQueue1 = new LinkedBlockingQueue<>();
        final int port1 = Integer.valueOf(args[1]);

        final String username2 = args[2];
        final String password2 = "mnsdg21g1742fz";
        final String phone2 = getRandomNum(10);
        final BlockingQueue<String> messageQueue2 = new LinkedBlockingQueue<>();
        final int port2 = Integer.valueOf(args[3]);

        final int msgRequestCount = Integer.valueOf(args[4]);
        int messageCount = msgRequestCount;

        final MyClient client1 = new MyClient(serverURL, metricManager);
        final MyClientServer clientServer1 = new MyClientServer("localhost", port1, messageQueue1, metricManager);
        final MyClient client2 = new MyClient(serverURL, metricManager);
        final MyClientServer clientServer2 = new MyClientServer("localhost", port2, messageQueue2, metricManager);

        clientServer1.run();
        clientServer2.run();

        client1.register(username1, password1, username1 + "@163.com", phone1);
        client2.register(username2, password2, username2 + "@163.com", phone2);

        client1.logOn(username1, password1, port1);
        client2.logOn(username2, password2, port2);

        final Random r = new Random();
        int failRequestCount = 0;

        while (messageCount > 0) {
            int msgCount1 = r.nextInt(5) + 1;
            while (messageCount > 0 && msgCount1 > 0) {
                try {
                    P2PMsgResponse p2PMsgResponse1 = client1.sendMessage(username1, username2, getRandomStr(10));

                    if (p2PMsgResponse1.getSuccess()) {
                        msgCount1 -= 1;
                        messageCount -= 1;
                    } else {
                        failRequestCount += 1;
                        logger.error("Failed to send message from {} to {}, because {}", username1, username2, p2PMsgResponse1.getFailReason());
                    }
                } catch (Exception e) {
                    failRequestCount += 1;
                    logger.error("Caught exception during sending message from {} to {}: {}", username1, username2, e.getMessage());
                }
            }

            int msgCount2 = r.nextInt(5) + 1;
            while (messageCount > 0 && msgCount2 > 0) {
                try {
                    P2PMsgResponse p2PMsgResponse2 = client2.sendMessage(username2, username1, getRandomStr(10));

                    if (p2PMsgResponse2.getSuccess()) {
                        msgCount2 -= 1;
                        messageCount -= 1;
                    } else {
                        failRequestCount += 1;
                        logger.error("Failed to send message from {} to {}, because {}", username2, username1, p2PMsgResponse2.getFailReason());
                    }
                } catch (Exception e) {
                    failRequestCount += 1;
                    logger.error("Caught exception during sending message from {} to {}: {}", username2, username1, e.getMessage());
                }
            }
        }

        final double failRequestRate = failRequestCount * 1.0 / msgRequestCount;
        logger.info("Total failed p2p message request number is {} and failed rate is {}", failRequestCount, failRequestRate);
    }

    private static String getRandomStr(int length) {
        final StringBuilder sb = new StringBuilder();
        final Random r = new Random();

        for (int i = 0; i < length; i++) {
            final int num = r.nextInt(26);
            sb.append((char) ('a' + num));
        }

        return sb.toString();
    }

    private static String getRandomNum(int length) {
        final StringBuilder sb = new StringBuilder();
        final Random r = new Random();

        for (int i = 0; i < length; i++) {
            final int num = r.nextInt(10);
            sb.append(num);
        }

        return sb.toString();
    }
}

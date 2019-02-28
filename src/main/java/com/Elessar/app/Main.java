package com.Elessar.app;

import com.Elessar.app.client.MyClient;
import com.Elessar.app.client.MyClientServer;
import com.Elessar.app.util.MetricManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Hans on 2/27/19.
 */
public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

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
        final MetricManager metricManager = new MetricManager("ClientMetric", 0);

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

        int messageCount = Integer.valueOf(args[4]);

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

        while (messageCount > 0) {
            int msgCount1 = r.nextInt(5) + 1;
            while (messageCount > 0 && msgCount1 > 0) {
                client1.sendMessage(username1, username2, getRandomStr(10));
                msgCount1 -= 1;
                messageCount -= 1;
            }

            int msgCount2 = r.nextInt(5) + 1;
            while (messageCount > 0 && msgCount2 > 0) {
                client2.sendMessage(username2, username1, getRandomStr(10));
                msgCount2 -= 1;
                messageCount -= 1;
            }
        }
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

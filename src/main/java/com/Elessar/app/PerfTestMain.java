package com.Elessar.app;

import com.Elessar.app.client.MyClient;
import com.Elessar.app.client.MyClientServer;
import com.Elessar.app.util.MetricManager;
import com.Elessar.proto.P2Pmsg.P2PMsgResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Hans on 2/27/19.
 */
public class PerfTestMain {
    private static final Logger logger = LogManager.getLogger(PerfTestMain.class);

    /**
     *
     * @param args [0] path to config file
     */
    public static void main(String[] args) throws Exception {
        String username = "";
        String password = "";
        String[] toUsers = new String[0];
        int port = -1;
        int msgCountPerUser = -1;
        int logOffCount = -1;
        long waitTime = -1; // in seconds

        try (final BufferedReader reader = new BufferedReader(new FileReader(args[0]))) {
            String line = "";
            int lineCount = 0;

            while ((line = reader.readLine()) != null) {
                if (line.equals("") || line.startsWith("#")) {
                    continue;
                }

                switch (lineCount) {
                    case 0:
                        username = line;
                        break;
                    case 1:
                        password = line;
                        break;
                    case 2:
                        port = Integer.valueOf(line);
                        break;
                    case 3:
                        toUsers = line.split("\\s*,\\s*");
                        break;
                    case 4:
                        msgCountPerUser = Integer.valueOf(line);
                        break;
                    case 5:
                        logOffCount = Integer.valueOf(line);
                        break;
                    case 6:
                        waitTime = Long.valueOf(line);
                        break;
                    default:
                        break;
                }

                lineCount += 1;
            }
        }

        if (username.equals("") || password.equals("") || toUsers.length == 0 || port == -1 || msgCountPerUser == -1
            || logOffCount == -1 || waitTime == -1) {

            logger.error("Fail to initialize using config settings");
            throw new RuntimeException("Fail to initialize using config settings");
        }

        // Setup client server
        final MetricManager metricManager = new MetricManager("ClientMetric", 100);
        final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

        final MyClientServer clientServer = new MyClientServer("localhost", port, messageQueue, metricManager);
        clientServer.run();


        // Setup client
        final String serverURL = new StringBuilder().append("http://127.0.0.1:9000").toString();
        final MyClient client = new MyClient(serverURL, metricManager);
        final String phone = getRandomNum(10);

        client.register(username, password, username + "@163.com", phone);

        // Sleep 5 sec, wait other users to register
        Thread.sleep(5000L);

        client.logOn(username, password, port);

        final Random r = new Random();
        int failRequestCount = 0;

        final int totalMsgCount = msgCountPerUser * toUsers.length;
        logOffCount = Math.min(logOffCount, totalMsgCount);

        final Set<Integer> logOffPoints = new HashSet<>();
        while (logOffCount > 0) {
            final int point = r.nextInt(totalMsgCount);

            if (logOffPoints.contains(point)) {
                continue;
            }

            logOffPoints.add(point);
            logOffCount -= 1;
        }

        for (int i = 0; i < toUsers.length; i++) {
            final String toUser = toUsers[i];
            int count = 0;

            while (count < msgCountPerUser) {
                final int sentCount = i * msgCountPerUser + count;
                if (logOffPoints.contains(sentCount)) {
                    try {
                        client.logOff(username);
                        Thread.sleep(waitTime * 1000);
                        client.logOn(username, password, port);
                    } catch (Exception e) {
                        logger.error("Caught exception during {} log on/log off: {}", username, e.getMessage());
                    }
                }

                try {
                    final String text = getRandomStr(10);
                    P2PMsgResponse p2PMsgResponse = client.sendMessage(username, toUser, text);

                    if (!p2PMsgResponse.getSuccess()) {
                        failRequestCount += 1;
                        logger.error("Failed to send message from {} to {}, because {}", username, toUser, p2PMsgResponse.getFailReason());
                    }

                } catch (Exception e) {
                    failRequestCount += 1;
                    logger.error("Caught exception during sending message from {} to {}: {}", username, toUser, e.getMessage());
                }

                count += 1;
            }
        }

        final double failRequestRate = failRequestCount * 1.0 / totalMsgCount;
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

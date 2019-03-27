package com.Elessar.app;

import com.Elessar.app.client.*;
import com.Elessar.app.util.MetricManager;
import com.Elessar.proto.Logoff.LogoffResponse;
import com.Elessar.proto.Logon.LogonResponse;
import com.Elessar.proto.P2Pmsg.P2PMsgResponse;
import com.Elessar.proto.Registration.RegistrationResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;

/**
 * Created by Hans on 1/13/19.
 */
public class ClientConsole {
    private static final int REGISTER = 1, LOG_ON = 2, P2P_MSG = 3, LOG_OFF = 4;
    private static final Logger logger = LogManager.getLogger(ClientConsole.class);
    private static String currUser = "";
    private static MsgQueue msgQueue = null;

    /**
     *
     * @param args [0] server address, [1] server port number, [2] client port number, [3] pull or push mode
     */
    public static void main(String[] args) {
        final MetricManager metricManager = new MetricManager("ClientMetric", 100);
        final String serverAddress = args[0];
        final int serverPort = Integer.valueOf(args[1]);
        final int clientPort = Integer.valueOf(args[2]);
        final String mode = args[3];

        if (!"push".equals(mode) && !"pull".equals(mode)) {
            throw new RuntimeException(mode + " mode is not supported");
        }

        final String serverURL = new StringBuilder().append("http://")
                                                           .append(serverAddress).append(":")
                                                           .append(serverPort).toString();

        final MyClient client = new MyClient(serverURL, metricManager);

        try (final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                try {
                    // user has logged on
                    if (isLogOn()) {
                        for (String message : msgQueue.poll(Duration.ofSeconds(1L))) {
                            System.out.println(message);
                        }
                    }

                    System.out.println("1. Register");
                    System.out.println("2. Log on");
                    System.out.println("3. Send Message");
                    System.out.println("4. Log off");

                    final int option = Integer.valueOf(stdin.readLine());
                    final String fromUser;
                    final String toUser;
                    final String password;
                    final String email;
                    final String phone;
                    final String text;
                    switch (option) {
                        case REGISTER:
                            System.out.println("Please enter username");
                            fromUser = stdin.readLine();

                            System.out.println("Please enter password");
                            password = stdin.readLine();

                            System.out.println("Please enter email");
                            email = stdin.readLine();

                            System.out.println("Please enter phone");
                            phone = stdin.readLine();

                            try {
                                RegistrationResponse registerResponse = client.register(fromUser, password, email, phone);
                                if (registerResponse.getSuccess()) {
                                    logger.info("User {} register successfully", fromUser);
                                } else {
                                    logger.error("User {} fail to register, because {}", fromUser, registerResponse.getFailReason());
                                }
                            } catch (Exception e) {
                                logger.error("Caught exception during user registration: {}", e.getMessage());
                            }

                            break;

                        case LOG_ON:
                            if (isLogOn()) {
                                System.out.printf("User %s already log on, please log off first before trying switching to other user account\n", currUser);
                                break;
                            }

                            System.out.println("Please enter username");
                            fromUser = stdin.readLine();

                            System.out.println("Please enter password");
                            password = stdin.readLine();

                            try {
                                final LogonResponse logonResponse = client.logOn(fromUser, password, clientPort);
                                if (logonResponse.getSuccess()) {
                                    logger.info("User {} log on successfully", fromUser);
                                    currUser = fromUser;
                                    msgQueue = "pull".equals(mode) ? new KafkaMsgQueue(currUser) : new BlockingMsgQueue(clientPort, metricManager);

                                } else {
                                    logger.error("User {} fail to log on, because {}", fromUser, logonResponse.getFailReason());
                                }
                            } catch (Exception e) {
                                logger.error("Caught exception during user log on: {}", e.getMessage());
                            }
                            break;


                        case P2P_MSG:
                            if (!isLogOn()) {
                                System.out.println("Please log on first !");
                                break;
                            }
                            System.out.println("Please enter user name to send message");
                            toUser = stdin.readLine();

                            System.out.println("Please enter text to send");
                            text = stdin.readLine();

                            try {
                                P2PMsgResponse p2pMsgResponse = client.sendMessage(currUser, toUser, text);

                                if (p2pMsgResponse.getSuccess()) {
                                    if (p2pMsgResponse.getIsDelivered()) {
                                        logger.info("Message sent to {} successfully", toUser);
                                    } else {
                                        logger.info("Message sending request received by server, will deliver to {} once {} is online", toUser, toUser);
                                    }
                                } else {
                                    logger.error("Message fail to send because {}, please retry !", p2pMsgResponse.getFailReason());
                                }
                            } catch (Exception e) {
                                logger.error("Caught exception during sending message: {}, please retry !", e.getMessage());
                            }

                            break;

                        case LOG_OFF:
                            if (!isLogOn()) {
                                System.out.println("Please log on first !");
                                break;
                            }

                            try {
                                final LogoffResponse logoffResponse = client.logOff(currUser);
                                if (logoffResponse.getSuccess()) {
                                    logger.info("User {} log off successfully", currUser);
                                    currUser = "";
                                    msgQueue = null;
                                    msgQueue.close();
                                } else {
                                    logger.info("User {} fail to log off, because {}", currUser, logoffResponse.getFailReason());
                                }
                            } catch (Exception e) {
                                logger.error("Caught exception during user log off: {}", e.getMessage());
                            }

                            break;

                        default:
                            System.out.println("Invalid option !");
                            break;
                    }

                } catch (Exception e) {
                    logger.error("Client failed because {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.error("Cannot read from system input because: {}", e.getMessage());
        } finally {
            msgQueue.close();
        }
    }

    private static boolean isLogOn() {
        return !currUser.isEmpty();
    }
}
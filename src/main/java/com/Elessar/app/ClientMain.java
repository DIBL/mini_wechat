package com.Elessar.app;

import com.Elessar.app.client.MyClient;
import com.Elessar.app.client.MyClientServer;
import com.Elessar.proto.Logoff.LogoffResponse;
import com.Elessar.proto.Logon.LogonResponse;
import com.Elessar.proto.P2Pmsg.P2PMsgResponse;
import com.Elessar.proto.Registration.RegistrationResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


/**
 * Created by Hans on 1/13/19.
 */
public class ClientMain {
    private static final int REGISTER = 1, LOG_ON = 2, P2P_MSG = 3, LOG_OFF = 4;
    private static final Logger logger = LogManager.getLogger(ClientMain.class);

    /**
     *
     * @param args [0] server address, [1] server port number, [2] client port number
     */
    public static void main(String[] args){

        final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

        final String serverAddress = args[0];
        final int serverPort = Integer.valueOf(args[1]);
        final int clientPort = Integer.valueOf(args[2]);


        final StringBuilder serverURL = new StringBuilder();
        serverURL.append("http://").append(serverAddress).append(":").append(serverPort);

        final MyClient client = new MyClient(serverURL.toString());
        final MyClientServer clientServer = new MyClientServer("localhost", clientPort, messageQueue);

        clientServer.run();

        String currUser = "";

        try (final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                try {
                    String message = messageQueue.poll(1L, TimeUnit.SECONDS);
                    if (message != null) {
                        System.out.println(message);
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
                                    logger.info("User {} fail to register, because {}", fromUser, registerResponse.getFailReason());
                                }
                            } catch (Exception e) {
                                logger.error("Caught exception during user registration: {}", e.getMessage());
                            }

                            break;

                        case LOG_ON:
                            if (!currUser.isEmpty()) {
                                System.out.printf("User %s already log on, please log off first before trying switching to other user account", currUser);
                                break;
                            }

                            System.out.println("Please enter username");
                            fromUser = stdin.readLine();

                            System.out.println("Please enter password");
                            password = stdin.readLine();

                            try {
                                final LogonResponse logonResponse = client.logOn(fromUser, password);
                                if (logonResponse.getSuccess()) {
                                    logger.info("User {} log on successfully", fromUser);
                                    currUser = fromUser;
                                } else {
                                    logger.info("User {} fail to log on, because {}", fromUser, logonResponse.getFailReason());
                                }
                            } catch (Exception e) {
                                logger.error("Caught exception during user log on: {}", e.getMessage());
                            }
                            break;


                        case P2P_MSG:
                            if (currUser.isEmpty()) {
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
                                    logger.debug("Message sent to {} successfully", toUser);
                                } else {
                                    logger.error("Message fail to send because {}, please retry !", p2pMsgResponse.getFailReason());
                                }
                            } catch (Exception e) {
                                logger.error("Caught exception during sending message: {}, please retry !", e.getMessage());
                            }

                            break;

                        case LOG_OFF:
                            if (currUser.isEmpty()) {
                                System.out.println("Please log on first !");
                                break;
                            }

                            try {
                                final LogoffResponse logoffResponse = client.logOff(currUser);
                                if (logoffResponse.getSuccess()) {
                                    logger.info("User {} log off successfully", currUser);
                                    currUser = "";
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
                    logger.info("Client failed because {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.info("Cannot read from system input because: {}", e.getMessage());
        }
    }
}
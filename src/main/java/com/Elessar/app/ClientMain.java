package com.Elessar.app;

import com.Elessar.app.client.MyClient;
import com.Elessar.app.client.MyClientServer;
import com.Elessar.proto.Logoff.LogoffResponse;
import com.Elessar.proto.Logon.LogonResponse;
import com.Elessar.proto.Logon.UnreadMsg;
import com.Elessar.proto.P2Pmessage.P2PMsgResponse;
import com.Elessar.proto.Registration.RegistrationResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;


/**
 * Created by Hans on 1/13/19.
 */
public class ClientMain {
    /**
     *
     * @param args [0] server address, [1] server port number, [2] client port number
     */
    public static void main(String[] args){
        final Logger logger = LogManager.getLogger(ClientMain.class);
        final Queue<String> unreadMsgs = new LinkedList<>();

        final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        final String serverAddress = args[0];
        final int serverPort = Integer.valueOf(args[1]);
        final String clientAddress = "127.0.0.1";               // Need to replace with real external IP later
        final int clientPort = Integer.valueOf(args[2]);


        final StringBuilder serverURL = new StringBuilder();
        serverURL.append("http://").append(serverAddress).append(":").append(serverPort);
        final StringBuilder clientURL = new StringBuilder();
        clientURL.append("http://").append(clientAddress).append(":").append(clientPort);

        final MyClient client = new MyClient(serverURL.toString());
        final MyClientServer clientServer = new MyClientServer("localhost", clientPort, unreadMsgs);

        while (true) {
            try {
                clientServer.run();

                while (!unreadMsgs.isEmpty()) {
                    System.out.println(unreadMsgs.poll());
                }

                boolean isLogOn = false;
                String currUser = "";
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
                    case 1:     //Register
                        System.out.println("Please enter username");
                        fromUser = stdin.readLine();
                        System.out.println("Please enter password");
                        password = stdin.readLine();
                        System.out.println("Please enter email");
                        email = stdin.readLine();
                        System.out.println("Please enter phone");
                        phone = stdin.readLine();
                        RegistrationResponse registerResponse = client.register(fromUser, password, email, phone);
                        if (registerResponse.getSuccess()) {
                            logger.info("User {} register successfully", fromUser);
                        } else {
                            logger.info("User {} fail to register, because {}", fromUser, registerResponse.getFailReason());
                        }
                        break;


                    case 2:     // Log on
                        if (isLogOn) {
                            System.out.printf("User %s already log on, please log off first before trying switching to other user account", currUser);
                            break;
                        }

                        System.out.println("Please enter username");
                        fromUser = stdin.readLine();
                        System.out.println("Please enter password");
                        password = stdin.readLine();
                        final LogonResponse logonResponse = client.logOn(fromUser, password, clientURL.toString());
                        if (logonResponse.getSuccess()) {
                            logger.info("User {} log on successfully", fromUser);
                            isLogOn = true;
                            currUser = fromUser;
                            for (UnreadMsg message : logonResponse.getMessagesList()) {
                                System.out.println(message.toString());
                            }
                        } else {
                            logger.info("User {} fail to log on, because {}", fromUser, logonResponse.getFailReason());
                        }
                        break;


                    case 3:     // Send Message
                        if (!isLogOn) {
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


                    case 4:     // Log off
                        if (!isLogOn) {
                            System.out.println("Please log on first !");
                            break;
                        }
                        final LogoffResponse logoffResponse = client.logOff(currUser);
                        if (logoffResponse.getSuccess()) {
                            logger.info("User {} log off successfully", currUser);
                            isLogOn = false;
                            currUser = "";
                        } else {
                            logger.info("User {} fail to log off, because {}", currUser, logoffResponse.getFailReason());
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
    }
}
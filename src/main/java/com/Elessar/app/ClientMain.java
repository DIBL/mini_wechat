package com.Elessar.app;

import com.Elessar.app.client.MyClient;
import com.Elessar.app.client.MyClientServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Created by Hans on 1/13/19.
 */
public class ClientMain {
    public static void main(String[] args){
        try {
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Please specify server IP address:");
            final String serverAddress = stdin.readLine();
            System.out.println("Please specify server port number:");
            final int serverPort = Integer.valueOf(stdin.readLine());
            System.out.println("Please specify client port number to build local server:");
            final int clientPort = Integer.valueOf(stdin.readLine());
            StringBuilder serverURL = new StringBuilder();
            serverURL.append("http://").append(serverAddress).append(":").append(serverPort);
            final String clientAddress = "127.0.0.1";
            StringBuilder clientURL = new StringBuilder();
            clientURL.append("http://").append(clientAddress).append(":").append(clientPort);
            final MyClient client = new MyClient(serverURL.toString());
            final MyClientServer clientServer = new MyClientServer("localhost", clientPort);
            clientServer.run();

            boolean isLogOn = false;
            String currUser = "";
            while (true) {
                System.out.println("1. Register");
                System.out.println("2. Log on");
                System.out.println("3. Send Message");
                System.out.println("4. Log off");
                int option = Integer.valueOf(stdin.readLine());
                String fromUser;
                String toUser;
                String password;
                String email;
                String phone;
                String text;
                switch (option) {
                    case 1:
                        System.out.println("Please enter username");
                        fromUser = stdin.readLine();
                        System.out.println("Please enter password");
                        password = stdin.readLine();
                        System.out.println("Please enter email");
                        email = stdin.readLine();
                        System.out.println("Please enter phone");
                        phone = stdin.readLine();
                        client.register(fromUser, password, email, phone);
                        break;
                    case 2:
                        if (isLogOn) {
                            System.out.printf("User %s already log on, please log off first before switch to other user account", currUser);
                            break;
                        }
                        System.out.println("Please enter username");
                        fromUser = stdin.readLine();
                        System.out.println("Please enter password");
                        password = stdin.readLine();
                        if (client.logOn(fromUser, password, clientURL.toString())) {
                            isLogOn = true;
                            currUser = fromUser;
                        }
                        break;
                    case 3:
                        if (!isLogOn) {
                            System.out.println("Please log on first !");
                            break;
                        }
                        System.out.println("Please enter user name to send message");
                        toUser = stdin.readLine();
                        System.out.println("Please enter text to send");
                        text = stdin.readLine();
                        client.sendMessage(currUser, toUser, text);
                        break;
                    case 4:
                        if (!isLogOn) {
                            System.out.println("Please log on first !");
                            break;
                        }
                        if (client.logOff(currUser)) {
                            isLogOn = false;
                            currUser = "";
                        }
                        break;
                    default:
                        System.out.println("Invalid option !");
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("Client failed because " + e.getMessage());
        }
    }
}

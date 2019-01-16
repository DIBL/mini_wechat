package com.Elessar.app;

import com.Elessar.app.client.MyClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by Hans on 1/13/19.
 */
public class ClientMain {
    public static void main(String[] args){

        try {
            final MyClient client = new MyClient(new URL("http://127.0.0.1:9000"));
//            client.echo();
//            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
//            System.out.println("Please enter a user name:");
//            String userName = stdin.readLine();
//            System.out.println("Please create a password for your account:");
//            String password = stdin.readLine();
//            System.out.println("Please enter your email:");
//            String email = stdin.readLine();
//            System.out.println("Please enter your phone number:");
//            String phoneNumber = stdin.readLine();
            client.register("abc", "123", "abc@126.com", "365-2873343");
            client.register("abc", "123", "abc@126.com", "365-2873343");
        } catch (IOException e) {
            System.out.println("Cannot get user input");
        }
    }

}

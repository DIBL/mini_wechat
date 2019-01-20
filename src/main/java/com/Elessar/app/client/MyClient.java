package com.Elessar.app.client;


import com.Elessar.proto.Logoff.LogoffRequest;
import com.Elessar.proto.Logoff.LogoffResponse;
import com.Elessar.proto.Logon.LogonRequest;
import com.Elessar.proto.Logon.LogonResponse;
import com.Elessar.proto.Registration.RegistrationResponse;
import com.Elessar.proto.Registration.RegistrationRequest;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.apache.http.protocol.HTTP;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;

/**
 * Created by Hans on 1/13/19.
 */
public class MyClient {
    private final URL hostURL;
    private final HttpClient httpClient;

    public MyClient(URL hostURL) {
        this.hostURL = hostURL;
        this.httpClient = new HttpClient(new NetHttpTransport().createRequestFactory());
    }

    public void logOn(String userName, String password) {
        try {
            final LogonRequest.Builder logonReq= LogonRequest.newBuilder();
            logonReq.setName(userName).setPassword(hash(password));

            final HttpResponse response = httpClient.post(new URL(hostURL.toString() + "/logon"), logonReq.build());
            final LogonResponse logonResponse = LogonResponse.parseFrom(response.getContent());

            if (logonResponse.getSuccess()) {
                System.out.printf("User %s Log On Successfully !\n", userName);
            } else {
                System.out.printf("User $s Fail to Log On, Because: %s\n", userName, logonResponse.getFailReason());
            }
        } catch (Exception e) {
            System.out.println("Log On request failed: " + e.getMessage());
        }
    }

    public void logOff(String userName) {
        try {
            final LogoffRequest.Builder logoffRequest= LogoffRequest.newBuilder();
            logoffRequest.setName(userName);

            final HttpResponse response = httpClient.post(new URL(hostURL.toString() + "/logoff"), logoffRequest.build());
            final LogoffResponse logoffResponse = LogoffResponse.parseFrom(response.getContent());

            if (logoffResponse.getSuccess()) {
                System.out.printf("User %s Log Off Successfully !\n", userName);
            } else {
                System.out.printf("User %s Fail to Log Off, Because: %s\n", userName, logoffResponse.getFailReason());
            }
        } catch (Exception e) {
            System.out.println("Log Off request failed: " + e.getMessage());
        }
    }

    public void register (String userName, String password, String email, String phoneNumber) {
        try {
            final RegistrationRequest.Builder registerRequest = RegistrationRequest.newBuilder();
            registerRequest.setName(userName).setPassword(hash(password)).setEmail(email);
            if (phoneNumber != null) {
                registerRequest.setPhoneNumber(phoneNumber);
            }

            final HttpResponse response = httpClient.post(new URL(hostURL.toString() + "/register"), registerRequest.build());
            final RegistrationResponse registerResponse = RegistrationResponse.parseFrom(response.getContent());

            if (registerResponse.getSuccess()) {
                System.out.printf("User %s is Successfully Registered !\n", userName);
            } else {
                System.out.printf("User %s Registration Failed Because: %s\n", userName, registerResponse.getFailReason());
            }
        } catch (Exception e) {
            System.out.println("Registration request failed: " + e.getMessage());
        }
    }

    public void echo () {
        try {
            httpClient.get(new URL(hostURL.toString() + "/echo"));
        } catch (Exception e) {
            System.out.println("Echo request failed: " + e.getMessage());
        }
    }

    private String hash(String password) throws Exception {
        final MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes());
        return DatatypeConverter.printHexBinary(md.digest());
    }
}
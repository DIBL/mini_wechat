package com.Elessar.app.client;


import com.Elessar.proto.Logoff.LogoffRequest;
import com.Elessar.proto.Logoff.LogoffResponse;
import com.Elessar.proto.Logon.LogonRequest;
import com.Elessar.proto.Logon.LogonResponse;
import com.Elessar.proto.Registration.RegistrationResponse;
import com.Elessar.proto.Registration.RegistrationRequest;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.protobuf.ProtoHttpContent;
import com.google.api.client.util.ExponentialBackOff;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;

/**
 * Created by Hans on 1/13/19.
 */
public class MyClient {
    private final URL hostURL;

    public MyClient(URL hostURL) {
        this.hostURL = hostURL;
    }

    public void logOn(String userName, String password) {
        try {
            final LogonRequest.Builder logonReq= LogonRequest.newBuilder();
            logonReq.setName(userName).setPassword(hash(password));

            final HttpRequestFactory REQ_FACTORY = new NetHttpTransport().createRequestFactory();
            final GenericUrl endURL = new GenericUrl(new URL(hostURL.toString() + "/logon"));
            final HttpContent content = new ProtoHttpContent(logonReq.build());
            final HttpRequest postRequest = REQ_FACTORY.buildPostRequest(endURL, content);
            final HttpResponse postResponse = postRequest.execute();
            final LogonResponse logonResponse = LogonResponse.parseFrom(postResponse.getContent());

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
        final LogoffRequest.Builder logoffReq= LogoffRequest.newBuilder();
        logoffReq.setName(userName);
        try {
            final HttpRequestFactory REQ_FACTORY = new NetHttpTransport().createRequestFactory();
            final GenericUrl endURL = new GenericUrl(new URL(hostURL.toString() + "/logoff"));
            final HttpContent content = new ProtoHttpContent(logoffReq.build());
            final HttpRequest postRequest = REQ_FACTORY.buildPostRequest(endURL, content);
            final HttpResponse postResponse = postRequest.execute();
            final LogoffResponse logoffResponse = LogoffResponse.parseFrom(postResponse.getContent());

            if (logoffResponse.getSuccess()) {
                System.out.printf("User %s Log Off Successfully !\n", userName);
            } else {
                System.out.printf("User %s Fail to Log Off, Because: %s\n", userName, logoffResponse.getFailReason());
            }
        } catch (IOException e) {
            System.out.println("Log Off request failed: " + e.getMessage());
        }
    }

    public void register (String userName, String password, String email, String phoneNumber) {
        try {
            final RegistrationRequest.Builder user = RegistrationRequest.newBuilder();
            user.setName(userName).setPassword(hash(password)).setEmail(email);
            if (phoneNumber != null) {
                user.setPhoneNumber(phoneNumber);
            }

            final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
            final HttpRequestFactory REQ_FACTORY = HTTP_TRANSPORT.createRequestFactory();
            final URL regURL = new URL(hostURL.toString() + "/register");
            final GenericUrl endURL = new GenericUrl(regURL);
            final HttpContent content = new ProtoHttpContent(user.build());
            final HttpRequest postRequest = REQ_FACTORY.buildPostRequest(endURL, content);
            final HttpResponse postResponse = postRequest.execute();
            final RegistrationResponse regResponse = RegistrationResponse.parseFrom(postResponse.getContent());

            if (regResponse.getSuccess()) {
                System.out.printf("User %s is Successfully Registered !\n", userName);
            } else {
                System.out.printf("User %s Registration Failed Because: %s\n", userName, regResponse.getFailReason());
            }
        } catch (Exception e) {
            System.out.println("Registration request failed: " + e.getMessage());
        }
    }

    public void echo () {
        try {
            final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
            final HttpRequestFactory REQ_FACTORY = HTTP_TRANSPORT.createRequestFactory();
            final URL echoURL = new URL(hostURL.toString() + "/echo");
            final GenericUrl endURL = new GenericUrl(echoURL);
            final HttpRequest getRequest = REQ_FACTORY.buildGetRequest(endURL);
            getRequest.execute();
        } catch (IOException e) {
            System.out.println("Echo request failed: " + e.getMessage());
        }
    }

    private String hash(String password) throws Exception {
        final MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes());
        return DatatypeConverter.printHexBinary(md.digest());
    }
}
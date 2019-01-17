package com.Elessar.app.client;


import com.Elessar.proto.Registration.RegistrationResponse;
import com.Elessar.proto.Registration.RegistrationRequest;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.protobuf.ProtoHttpContent;
import java.io.IOException;
import java.net.URL;

/**
 * Created by Hans on 1/13/19.
 */
public class MyClient {
    private final URL hostURL;

    public MyClient(URL hostURL) {
        this.hostURL = hostURL;
    }

    public void register (String userName, String password, String email, String phoneNumber) {
        final RegistrationRequest.Builder user = RegistrationRequest.newBuilder();
        user.setName(userName).setPassword(password).setEmail(email);
        if (phoneNumber != null) {
            user.setPhoneNumber(phoneNumber);
        }

        try {
            final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
            final HttpRequestFactory REQ_FACTORY = HTTP_TRANSPORT.createRequestFactory();
            final URL regURL = new URL(hostURL.toString() + "/register");
            final GenericUrl endURL = new GenericUrl(regURL);
            final HttpContent content = new ProtoHttpContent(user.build());
            final HttpRequest postRequest = REQ_FACTORY.buildPostRequest(endURL, content);
            final HttpResponse postResponse = postRequest.execute();
            final RegistrationResponse regResponse = RegistrationResponse.parseFrom(postResponse.getContent());

            if (regResponse.getSuccess()) {
                System.out.println("User is Successfully Registered !");
            } else {
                System.out.println("User Registration Failed Because: " + regResponse.getFailReason());
            }
        } catch (IOException e) {
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
}
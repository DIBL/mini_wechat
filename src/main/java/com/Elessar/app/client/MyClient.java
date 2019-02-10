package com.Elessar.app.client;


import com.Elessar.proto.Logoff.LogoffRequest;
import com.Elessar.proto.Logoff.LogoffResponse;
import com.Elessar.proto.Logon.LogonRequest;
import com.Elessar.proto.Logon.LogonResponse;
import com.Elessar.proto.Registration.RegistrationResponse;
import com.Elessar.proto.Registration.RegistrationRequest;
import com.Elessar.proto.P2Pmessage.P2PMsgRequest;
import com.Elessar.proto.P2Pmessage.P2PMsgResponse;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.xml.bind.DatatypeConverter;
import java.net.URL;
import java.security.MessageDigest;

/**
 * Created by Hans on 1/13/19.
 */
public class MyClient {
    private static final Logger logger = LogManager.getLogger(MyClient.class);
    private final String hostURL;
    private final HttpClient httpClient;
    public MyClient(String hostURL) {
        this.hostURL = hostURL;
        this.httpClient = new HttpClient(new NetHttpTransport().createRequestFactory());
    }

    public P2PMsgResponse sendMessage (String fromUser, String toUser, String text) throws Exception {
        final P2PMsgRequest.Builder sendMsgRequest = P2PMsgRequest.newBuilder().setFromUser(fromUser)
                                                                                 .setToUser(toUser)
                                                                                 .setText(text)
                                                                                 .setTimestamp(System.currentTimeMillis());
        final HttpResponse response = httpClient.post(new URL(hostURL + "/p2pMessage"), sendMsgRequest.build());
        return P2PMsgResponse.parseFrom(response.getContent());
    }

    public LogonResponse logOn(String userName, String password, String clientURL) {
        LogonResponse logonResponse = null;
        try {
            // Log on with credentials and client current IP
            final LogonRequest.Builder logonRequest = LogonRequest.newBuilder();
            logonRequest.setName(userName).setPassword(hash(password)).setClientURL(clientURL);

            final HttpResponse response = httpClient.post(new URL(hostURL + "/logon"), logonRequest.build());
            logonResponse = LogonResponse.parseFrom(response.getContent());
        } catch (Exception e) {
            logger.error("Caught exception during user log on: {}", e.getMessage());
        }
        return logonResponse;
    }

    public LogoffResponse logOff(String userName) {
        LogoffResponse logoffResponse = null;
        try {
            final LogoffRequest.Builder logoffRequest= LogoffRequest.newBuilder();
            logoffRequest.setName(userName);

            final HttpResponse response = httpClient.post(new URL(hostURL + "/logoff"), logoffRequest.build());
            logoffResponse = LogoffResponse.parseFrom(response.getContent());
        } catch (Exception e) {
            logger.error("Caught exception during user log off: {}", e.getMessage());
        }
        return logoffResponse;
    }

    public RegistrationResponse register (String userName, String password, String email, String phoneNumber) {
        RegistrationResponse registerResponse = null;
        try {
            final RegistrationRequest.Builder registerRequest = RegistrationRequest.newBuilder();
            registerRequest.setName(userName).setPassword(hash(password)).setEmail(email);
            if (phoneNumber != null) {
                registerRequest.setPhoneNumber(phoneNumber);
            }

            final HttpResponse response = httpClient.post(new URL(hostURL + "/register"), registerRequest.build());
            registerResponse = RegistrationResponse.parseFrom(response.getContent());
        } catch (Exception e) {
            logger.error("Caught exception during user registration: {}", e.getMessage());
        }
        return registerResponse;
    }

    public void echo () {
        try {
            httpClient.get(new URL(hostURL + "/echo"));
        } catch (Exception e) {
            logger.error("Caught exception during echo request: {}", e.getMessage());
        }
    }

    private String hash(String password) throws Exception {
        final MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes());
        return DatatypeConverter.printHexBinary(md.digest());
    }
}
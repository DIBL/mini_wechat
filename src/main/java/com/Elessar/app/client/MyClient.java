package com.Elessar.app.client;


import com.Elessar.proto.Logoff.LogoffRequest;
import com.Elessar.proto.Logoff.LogoffResponse;
import com.Elessar.proto.Logon.LogonRequest;
import com.Elessar.proto.Logon.LogonResponse;
import com.Elessar.proto.P2Pmsg;
import com.Elessar.proto.Registration.RegistrationResponse;
import com.Elessar.proto.Registration.RegistrationRequest;
import com.Elessar.proto.P2Pmsg.P2PMsgRequest;
import com.Elessar.proto.P2Pmsg.P2PMsgResponse;
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

    public P2PMsgResponse sendMessage (String fromUser,
                                       String toUser,
                                       String text) throws Exception {
        final P2PMsgRequest.Builder sendMsgRequest = P2PMsgRequest.newBuilder().setFromUser(fromUser)
                                                                               .setToUser(toUser)
                                                                               .addMessage(P2Pmsg.Message.newBuilder()
                                                                                       .setText(text)
                                                                                       .setTimestamp(0L));  // Set time stamp at server side

        final HttpResponse response = httpClient.post(new URL(hostURL + "/p2pMessage"), sendMsgRequest.build());
        return P2PMsgResponse.parseFrom(response.getContent());
    }

    public LogonResponse logOn(String userName,
                               String password) throws Exception {
            final LogonRequest.Builder logonRequest = LogonRequest.newBuilder().setName(userName)
                                                                               .setPassword(hash(password));

            final HttpResponse response = httpClient.post(new URL(hostURL + "/logon"), logonRequest.build());
            return LogonResponse.parseFrom(response.getContent());
    }

    public LogoffResponse logOff(String userName) throws Exception {
            final LogoffRequest.Builder logoffRequest= LogoffRequest.newBuilder().setName(userName);

            final HttpResponse response = httpClient.post(new URL(hostURL + "/logoff"), logoffRequest.build());
            return LogoffResponse.parseFrom(response.getContent());
    }

    public RegistrationResponse register (String userName,
                                          String password,
                                          String email,
                                          String phoneNumber) throws Exception {
        final RegistrationRequest.Builder registerRequest = RegistrationRequest.newBuilder().setName(userName)
                                                                                            .setPassword(hash(password))
                                                                                            .setEmail(email);
        if (phoneNumber != null) {
            registerRequest.setPhoneNumber(phoneNumber);
        }

        final HttpResponse response = httpClient.post(new URL(hostURL + "/register"), registerRequest.build());
        return RegistrationResponse.parseFrom(response.getContent());
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
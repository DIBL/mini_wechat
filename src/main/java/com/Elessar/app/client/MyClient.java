package com.Elessar.app.client;


import com.Elessar.proto.Logoff.LogoffRequest;
import com.Elessar.proto.Logoff.LogoffResponse;
import com.Elessar.proto.Logon;
import com.Elessar.proto.Logon.LogonRequest;
import com.Elessar.proto.Logon.LogonResponse;
import com.Elessar.proto.Registration.RegistrationResponse;
import com.Elessar.proto.Registration.RegistrationRequest;
import com.Elessar.proto.Sendmessage.SendMsgRequest;
import com.Elessar.proto.Sendmessage.SendMsgResponse;
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
    private static final Logger msglogger = LogManager.getLogger("messages");
    private final String hostURL;
    private final HttpClient httpClient;
    public MyClient(String hostURL) {
        this.hostURL = hostURL;
        this.httpClient = new HttpClient(new NetHttpTransport().createRequestFactory());
    }

    public void sendMessage(String fromUser, String toUser, String text) {
        try {
            final SendMsgRequest.Builder sendMsgRequest = SendMsgRequest.newBuilder();
            sendMsgRequest.setFromUser(fromUser).setToUser(toUser).setText(text).setTimestamp(System.currentTimeMillis());

            final HttpResponse response = httpClient.post(new URL(hostURL + "/sendMessage"), sendMsgRequest.build());
            final SendMsgResponse sendMsgResponse = SendMsgResponse.parseFrom(response.getContent());

            if (sendMsgResponse.getSuccess()) {
                logger.info("Message sent from {} to {} successfully", fromUser, toUser);
            } else {
                logger.info("Message sent failed, because {}", sendMsgResponse.getFailReason());
            }
        } catch (Exception e) {
            logger.error("Caught exception during sending message: {}", e.getMessage());
        }
    }

    public boolean logOn(String userName, String password, String clientURL) {
        boolean isSuccess = false;
        try {
            // Log on with credentials and current IP
            final LogonRequest.Builder logonRequest = LogonRequest.newBuilder();
            logonRequest.setName(userName).setPassword(hash(password)).setClientURL(clientURL);

            final HttpResponse response = httpClient.post(new URL(hostURL + "/logon"), logonRequest.build());
            final LogonResponse logonResponse = LogonResponse.parseFrom(response.getContent());

            if (logonResponse.getSuccess()) {
                logger.info("User {} log on successfully", userName);
                isSuccess = true;
                for (Logon.UnreadMsg message : logonResponse.getMessagesList()) {
                    System.out.println(message.toString());
                }
            } else {
                logger.info("User {} fail to log on, because {}", userName, logonResponse.getFailReason());
            }
        } catch (Exception e) {
            logger.error("Caught exception during user log on: {}", e.getMessage());
        }
        return isSuccess;
    }

    public boolean logOff(String userName) {
        boolean isSuccess = false;
        try {
            final LogoffRequest.Builder logoffRequest= LogoffRequest.newBuilder();
            logoffRequest.setName(userName);

            final HttpResponse response = httpClient.post(new URL(hostURL + "/logoff"), logoffRequest.build());
            final LogoffResponse logoffResponse = LogoffResponse.parseFrom(response.getContent());

            if (logoffResponse.getSuccess()) {
                logger.info("User {} log off successfully", userName);
                isSuccess = true;
            } else {
                logger.info("User {} fail to log off, because {}", userName, logoffResponse.getFailReason());
            }
        } catch (Exception e) {
            logger.error("Caught exception during user log off: {}", e.getMessage());
        }
        return isSuccess;
    }

    public void register (String userName, String password, String email, String phoneNumber) {
        try {
            final RegistrationRequest.Builder registerRequest = RegistrationRequest.newBuilder();
            registerRequest.setName(userName).setPassword(hash(password)).setEmail(email);
            if (phoneNumber != null) {
                registerRequest.setPhoneNumber(phoneNumber);
            }

            final HttpResponse response = httpClient.post(new URL(hostURL + "/register"), registerRequest.build());
            final RegistrationResponse registerResponse = RegistrationResponse.parseFrom(response.getContent());

            if (registerResponse.getSuccess()) {
                logger.info("User {} register successfully", userName);
            } else {
                logger.info("User {} fail to register, because {}", userName, registerResponse.getFailReason());
            }
        } catch (Exception e) {
            logger.error("Caught exception during user registration: {}", e.getMessage());
        }
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
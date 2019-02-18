package com.Elessar.app.client;


import com.Elessar.app.util.HttpClient;
import com.Elessar.app.util.Metric;
import com.Elessar.app.util.MetricManager;
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
    private static final String CLIENT = "client", P2P_MSG = "p2pMsg", LOGON = "logon", LOGOFF = "logoff", REGISTER = "register";
    private static final Logger logger = LogManager.getLogger(MyClient.class);
    private final String hostURL;
    private final HttpClient httpClient;
    private final MetricManager metricManager;

    public MyClient(String hostURL, MetricManager metricManager) {
        this.hostURL = hostURL;
        this.metricManager = metricManager;
        this.httpClient = new HttpClient(new NetHttpTransport().createRequestFactory());
    }

    public P2PMsgResponse sendMessage (String fromUser,
                                       String toUser,
                                       String text) throws Exception {
        final Metric metric = new Metric(metricManager, new StringBuilder().append(CLIENT).append(".")
                                                                           .append(P2P_MSG).toString());

        try {
            metric.timerStart();
        } catch (Exception e) {
            logger.debug("Caught exception when trying to start timer during sending message: {}", e.getMessage());
        }

        final P2PMsgRequest.Builder sendMsgRequest = P2PMsgRequest.newBuilder().setFromUser(fromUser)
                                                                               .setToUser(toUser)
                                                                               .addMessage(P2Pmsg.Message.newBuilder()
                                                                                       .setText(text)
                                                                                       .setTimestamp(0L));  // Set time stamp at server side

        final HttpResponse response = httpClient.post(new URL(hostURL + "/p2pMessage"), sendMsgRequest.build());

        try {
            metric.timerStop();
        } catch (Exception e) {
            logger.debug("Caught exception when trying to stop timer during sending message: {}", e.getMessage());
        }

        return P2PMsgResponse.parseFrom(response.getContent());
    }


    public LogonResponse logOn(String userName,
                               String password,
                               int clientPort) throws Exception {
        final Metric metric = new Metric(metricManager, new StringBuilder().append(CLIENT).append(".")
                                                                           .append(LOGON).toString());
        try {
            metric.timerStart();
        } catch (Exception e) {
            logger.debug("Caught exception when trying to start timer during log on: {}", e.getMessage());
        }

        final LogonRequest.Builder logonRequest = LogonRequest.newBuilder().setName(userName)
                                                                           .setPassword(hash(password))
                                                                           .setPort(clientPort);

        final HttpResponse response = httpClient.post(new URL(hostURL + "/logon"), logonRequest.build());

        try {
            metric.timerStop();
        } catch (Exception e) {
            logger.debug("Caught exception when trying to stop timer during log on: {}", e.getMessage());
        }

        return LogonResponse.parseFrom(response.getContent());
    }


    public LogoffResponse logOff(String userName) throws Exception {
        final Metric metric = new Metric(metricManager, new StringBuilder().append(CLIENT).append(".")
                                                                           .append(LOGOFF).toString());
        try {
            metric.timerStart();
        } catch (Exception e) {
            logger.debug("Caught exception when trying to start timer during log off: {}", e.getMessage());
        }

        final LogoffRequest.Builder logoffRequest= LogoffRequest.newBuilder().setName(userName);

        final HttpResponse response = httpClient.post(new URL(hostURL + "/logoff"), logoffRequest.build());

        try {
            metric.timerStop();
        } catch (Exception e) {
            logger.debug("Caught exception when trying to stop timer during log off: {}", e.getMessage());
        }

        return LogoffResponse.parseFrom(response.getContent());
    }


    public RegistrationResponse register (String userName,
                                          String password,
                                          String email,
                                          String phoneNumber) throws Exception {
        final Metric metric = new Metric(metricManager, new StringBuilder().append(CLIENT).append(".")
                                                                           .append(REGISTER).toString());

        try {
            metric.timerStart();
        } catch (Exception e) {
            logger.debug("Caught exception when trying to start timer during register: {}", e.getMessage());
        }

        final RegistrationRequest.Builder registerRequest = RegistrationRequest.newBuilder().setName(userName)
                                                                                            .setPassword(hash(password))
                                                                                            .setEmail(email)
                                                                                            .setPhoneNumber(phoneNumber);

        final HttpResponse response = httpClient.post(new URL(hostURL + "/register"), registerRequest.build());

        try {
            metric.timerStop();
        } catch (Exception e) {
            logger.debug("Caught exception when trying to stop timer during register: {}", e.getMessage());
        }

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
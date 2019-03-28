package com.Elessar.app.server;

import com.Elessar.proto.P2Pmsg.P2PMsgResponse;

import java.util.List;

/**
 * Created by Hans on 1/27/19.
 */
public interface MsgSender {
    final static String MSG_SENDER = "msgSender", SEND = "send";

    P2PMsgResponse send(List<Message> messages, String toUser) throws Exception;

    void close();
}

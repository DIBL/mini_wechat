package com.Elessar.app.server;

import com.Elessar.app.client.HttpClient;
import com.Elessar.proto.P2Pmessage.P2PMsgResponse;

/**
 * Created by Hans on 1/27/19.
 */
public interface MsgSender {

    P2PMsgResponse send(Message message, String URL, HttpClient httpClient);
}

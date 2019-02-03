package com.Elessar.app.server;

import com.Elessar.app.client.HttpClient;

/**
 * Created by Hans on 1/27/19.
 */
public interface MsgSender {

    void send(Message message, String URL, HttpClient httpClient);
}

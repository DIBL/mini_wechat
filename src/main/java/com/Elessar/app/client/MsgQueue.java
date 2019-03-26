package com.Elessar.app.client;

import java.time.Duration;
import java.util.List;

/**
 * Created by Hans on 3/18/19.
 */
public interface MsgQueue {

    // All implementations should not return null
    List<String> poll(Duration timeout) throws Exception;

    void close();
}

package com.Elessar.app.client;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.protobuf.ProtoHttpContent;
import com.google.protobuf.GeneratedMessageV3;

import java.net.URL;

/**
 * Created by Hans on 1/19/19.
 */
public class HttpClient {

    public static HttpResponse execute(URL url, GeneratedMessageV3 request) throws Exception {
        final GenericUrl endURL = new GenericUrl(url);
        final HttpContent content = new ProtoHttpContent(request);
        final HttpRequest postRequest = new NetHttpTransport().createRequestFactory().buildPostRequest(endURL, content);
        return postRequest.execute();
    }
}

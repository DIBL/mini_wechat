package com.Elessar.app.util;

import com.google.api.client.http.*;
import com.google.api.client.http.protobuf.ProtoHttpContent;
import com.google.protobuf.MessageLite;
import java.net.URL;

/**
 * Created by Hans on 1/19/19.
 */
public class HttpClient {
    private final HttpRequestFactory httpRequestFactory;

    public HttpClient(HttpRequestFactory httpRequestFactory) {
        this.httpRequestFactory = httpRequestFactory;
    }

    public HttpResponse post(URL url, MessageLite request) throws Exception {
        final GenericUrl endURL = new GenericUrl(url);
        final HttpContent content = new ProtoHttpContent(request);
        final HttpRequest postRequest = httpRequestFactory.buildPostRequest(endURL, content).setThrowExceptionOnExecuteError(false);
        return postRequest.execute(); //throw exception for HTTP error code (non-2XX)
    }

    public HttpResponse get(URL url) throws Exception {
        final GenericUrl endURL = new GenericUrl(url);
        final HttpRequest getRequest = httpRequestFactory.buildGetRequest(endURL).setThrowExceptionOnExecuteError(false);
        return getRequest.execute(); //throw exception for HTTP error code (non-2XX)
    }

}

package com.slj.io.proxy;

import lombok.Data;

import java.net.URL;

/**
 * Created by slj on 2019-05-15
 */

@Data
public class HttpRequest {

    private String method;

    private URL url;

    private String version;

    private String host;

    private String accept;

    private String proxyConnection;

    private String cookie;

    private String userAgent;

    private String acceptLanguage;

    private String acceptEncoding;

    private String connection;

    private String origin;

    private boolean finished;

    int getPort(){
        return this.url.getPort()!=-1?this.url.getPort():
            this.getUrl().getProtocol().equals("https")?443:80;
    }

    boolean isHttps(){
        return method.equalsIgnoreCase("CONNECT");
        //return this.url.getProtocol().equals("https");
    }

}

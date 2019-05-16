package com.slj.proxy;

import java.net.URL;
import lombok.Data;

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

    int getPort(){
        return this.url.getPort()!=-1?this.url.getPort():
            this.getUrl().getProtocol().equals("https")?443:80;
    }

    boolean isHttps(){
        return this.url.getProtocol().equals("https");
    }

}

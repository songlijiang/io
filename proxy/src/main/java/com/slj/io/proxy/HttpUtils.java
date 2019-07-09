package com.slj.io.proxy;

import io.netty.buffer.ByteBuf;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author songlijiang
 * @version 2019/6/24 14:44
 */
public class HttpUtils {

    static HttpRequest convertHttpRequest(ByteBuf in)  {

        String httpRequestString = in.toString(Constant.charset);
        in.readerIndex(in.writerIndex());
        String [] requestContext =  httpRequestString.split("\r\n");
        String urlContext = requestContext[0];
        String [] urlContexts = urlContext.split(" ");
        if(urlContexts.length<3){
            System.err.println(httpRequestString);
        }
        HttpRequest httpRequest = new HttpRequest();

        httpRequest.setMethod(urlContexts[0]);
        String urlString =urlContexts[1];
        if(urlContexts.length>2){
            httpRequest.setVersion(urlContexts[2]);
        }
        try {
            httpRequest.setUrl(getUrl(urlString));
        } catch (Exception e) {
            System.out.println(e);
        }

        Set<String> fieldNames = Arrays.stream(HttpRequest.class.getDeclaredFields()).map(Field::getName).collect(Collectors.toSet());
        Arrays.asList(requestContext).subList(1,requestContext.length).stream().filter(e->e.contains(": ")).forEach(e->{

            List<String> list = Arrays.asList(e.split(": "));
            String key = list.get(0).substring(0,1).toLowerCase()+list.get(0).substring(1).replaceAll("-","");
            if(!fieldNames.contains(key)){
                return;
            }
            String value = e.substring(key.length()+": ".length());
            try {
                Field field = HttpRequest.class.getDeclaredField(key);
                field.setAccessible(true);
                field.set(httpRequest,value);
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
            } catch (NoSuchFieldException e1) {
                e1.printStackTrace();
            }
        });
        httpRequest.setFinished(true);
        return httpRequest;
    }

    private static URL getUrl(String origin) throws MalformedURLException {
        String temp =origin;
        if(!origin.startsWith("https")&&!origin.startsWith("http")){
            if(origin.endsWith("443")){
                temp="https://"+temp;
            }else {
                temp="http://"+temp;
            }
        }
        return new URL(temp);

    }

}

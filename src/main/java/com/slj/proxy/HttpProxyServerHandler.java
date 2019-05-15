package com.slj.proxy;

import com.slj.Constant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * @author songlijiang
 * @version 2019-05-08
 */
@ChannelHandler.Sharable
public class HttpProxyServerHandler extends ChannelInboundHandlerAdapter {



    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        String requestString = in.toString(Constant.charset);
        System.out.println("Server received: "+requestString);
        HttpRequest httpRequest = convertHttpRequest(requestString);
        ctx.writeAndFlush(getResult().getBytes(Constant.charset));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush("").addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private HttpRequest convertHttpRequest(String httpRequestString) throws MalformedURLException {
        String [] requestContext =  httpRequestString.split("\r\n");
        String urlContext = requestContext[0];
        String [] urlContexts = urlContext.split(" ");

        String method = urlContexts[0];
        String urlString =urlContexts[1];
        String urlVersion = urlContexts[2];
        URL url = new URL(urlString);

        HttpRequest httpRequest = new HttpRequest();
        httpRequest.setMethod(method);
        httpRequest.setVersion(urlVersion);
        httpRequest.setUrl(url);

        Arrays.asList(requestContext).subList(1,requestContext.length).stream().filter(e->e.contains(": ")).forEach(e->{

            List<String> list = Arrays.asList(e.split(": "));
            String key = list.get(0).substring(0,1).toLowerCase()+list.get(0).substring(1).replaceAll("-","");
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

        return httpRequest;
    }

    private String doRequest(){
        return "HTTP/1.1 200 OK\r\n"
            + "Date: Mon, 27 Jul 2009 12:28:53 GMT\r\n"
            + "Server: Apache/2.2.14 (Win32)\r\n"
            + "Last-Modified: Wed, 22 Jul 2009 19:15:56 GMT\r\n"
            + "Content-Length: 88\r\n"
            + "Content-Type: text/html\r\n"
            + "Connection: Closed\r\n\r\n"+
            "<html>\n"
            + "<body>\n"
            + "<h1>Hello, World!</h1>\n"
            + "</body>\n"
            + "</html>";
    }
    public  String getResult(){

        List<String> headers = getHeader();
        StringBuffer result=new StringBuffer();
        for (String header : headers){
            result.append(header);
            result.append("\r\n");
        }
        result.append("\r\n");
        result.append("hello !");
        result.append("\r\n");
        System.out.println(result);
        return result.toString();

    }


    private static List<String> getHeader(){
        return  Arrays.asList("HTTP/1.1 200 OK",
            "Server: SimpleWebServer",
            "Content-Type: text/html;charset=UTF-8");
    }
}

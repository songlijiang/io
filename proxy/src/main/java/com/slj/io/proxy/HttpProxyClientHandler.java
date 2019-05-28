package com.slj.io.proxy;

import com.slj.Constant;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author songlijiang
 * @version 2019-05-08
 */
@ChannelHandler.Sharable
public class HttpProxyClientHandler extends ChannelInboundHandlerAdapter {


    private Channel clientChannel;

    private Channel remoteChannel;

    private HttpRequest httpRequest;

    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        String requestString = in.toString(Constant.charset);
        System.out.println("received from client \n"+requestString);


        if(httpRequest!=null &&httpRequest.isFinished()){
            remoteChannel.writeAndFlush(msg);
            return;
        }

        httpRequest = convertHttpRequest(in);
        clientChannel.config().setAutoRead(false); // disable AutoRead until remote connection is ready

        if (httpRequest.isHttps()) { // if https, respond 200 to create tunnel
            clientChannel.writeAndFlush(Unpooled.wrappedBuffer("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes()));
        }

        Bootstrap b = new Bootstrap();
        b.group(clientChannel.eventLoop()) // use the same EventLoop
            .channel(clientChannel.getClass())
            .handler(new HttpProxyRemoteHandler(clientChannel));
        ChannelFuture f = b.connect(httpRequest.getUrl().getHost(), httpRequest.getPort());
        remoteChannel = f.channel();

        f.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                clientChannel.config().setAutoRead(true); // connection is ready, enable AutoRead
                remoteChannel.writeAndFlush(in);  //send request
            } else {
                in.release();
                clientChannel.close();
            }
        });
    }



    @Override public void channelActive(ChannelHandlerContext ctx)  {
        clientChannel=ctx.channel();
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)  {
        cause.printStackTrace();
        clientChannel.close();
    }

    private HttpRequest convertHttpRequest(ByteBuf in)  {

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

    private URL getUrl(String origin) throws MalformedURLException {
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

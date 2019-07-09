package com.slj.io.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;

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

        httpRequest = HttpUtils.convertHttpRequest(in);
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





}

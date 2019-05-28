package com.slj.io.proxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by slj on 2019-05-16
 */
public class HttpProxyRemoteHandler extends ChannelInboundHandlerAdapter {


    private Channel clientChannel;

    private Channel remoteChannel;

    public HttpProxyRemoteHandler(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        clientChannel.writeAndFlush(msg);  //just  forward
    }

    @Override public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.remoteChannel=ctx.channel();
    }

    @Override public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        clientChannel.close();
    }

    @Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        clientChannel.close();
    }
}

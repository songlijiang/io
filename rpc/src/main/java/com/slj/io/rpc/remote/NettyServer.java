package com.slj.io.rpc.remote;

import com.slj.io.rpc.domain.InvokeRequest;
import com.slj.io.rpc.domain.InvokeResponse;
import com.slj.io.rpc.remote.pipeline.BizPipeline;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author songlijiang
 * @version 2019/6/26 15:09
 */
public class NettyServer implements Server {

    private int port=8080;
    private ServerBootstrap serverBootstrap = new ServerBootstrap();
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

    private ChannelFuture channelFuture;

    @Override
    public void start() {
        serverBootstrap.group(eventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .localAddress(port)
                .childHandler(new ChannelInitializer<Channel>() {

                    @Override
                    protected void initChannel(Channel ch) throws Exception {

                         ch.pipeline().addLast(new BizPipeline());
                    }
                });
        try {
            channelFuture = serverBootstrap.bind().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        eventLoopGroup.shutdownGracefully();
        channelFuture.channel().close();
    }

    @Override
    public InvokeResponse invoke(InvokeRequest invokeRequest) {
        return null;
    }
}

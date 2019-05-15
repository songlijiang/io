package com.slj.proxy;

import com.slj.Constant;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.net.InetSocketAddress;

/**
 * @author songlijiang
 * @version 2019-05-08
 */
public class HttpProxyServer {


    public static void main(String[] args) {
        try {
            new HttpProxyServer().run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void run() throws InterruptedException {

        final HttpProxyServerHandler httpProxyServerHandler = new HttpProxyServerHandler();
        NioEventLoopGroup nioEventLoopGroup =new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap
                    .group(nioEventLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(Constant.serverPort))
                    .childHandler(new ChannelInitializer<Channel>() {

                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline().addLast(httpProxyServerHandler);
                        }
                    });

            ChannelFuture channelFuture = serverBootstrap.bind().sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            nioEventLoopGroup.shutdownGracefully().sync();
        }

    }
}

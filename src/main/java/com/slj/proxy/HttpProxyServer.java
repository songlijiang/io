package com.slj.proxy;

import com.slj.Constant;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.net.InetSocketAddress;

/**
 * @author songlijiang
 * @version 2019-05-08
 */
public class HttpProxyServer {


    public static void main(String[] args) {
        /*new Thread(()-> {
            try {
                new HttpProxyServer().run();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();*/

        new Thread(() -> {
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline().addLast(new HttpProxyClientHandler());
                        }
                    }).bind(Constant.serverPort).sync().channel().closeFuture().sync();
            } catch (InterruptedException e) {

            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }).start();
    }




    public void run() throws InterruptedException {

        final HttpProxyClientHandler httpProxyClientHandler = new HttpProxyClientHandler();
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
                            channel.pipeline().addLast(httpProxyClientHandler);
                        }
                    });

            ChannelFuture channelFuture = serverBootstrap.bind().sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            nioEventLoopGroup.shutdownGracefully().sync();
        }

    }
}

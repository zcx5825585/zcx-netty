package org.zcx.netty.handler.bootstrap;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zcx.netty.handler.dynamicHandler.UdpHandler;

@Component
public class NettyUdpServerRunner {
    private static final Logger log = LoggerFactory.getLogger(NettyUdpServerRunner.class);
//    @Resource
//    private ServerGatewayHandler gatewayHandler;

    public static int port = 18020;

    public static void main(String[] args) throws InterruptedException {
//    }
//    public void runHandlerAsServer(int port, DynamicHandler handler) throws Exception {
        //NioEventLoopGroup是用来处理IO操作的多线程事件循环器
        EventLoopGroup bossGroup = new NioEventLoopGroup();  // 用来接收进来的连接
        Bootstrap server = new Bootstrap();//是一个启动NIO服务的辅助启动类
        server.group(bossGroup)
                .channel(NioDatagramChannel.class)  // 这里告诉Channel如何接收新的连接
                .handler(new UdpHandler());
        server.option(ChannelOption.SO_BROADCAST, true);
        ChannelFuture f = server.bind(port).sync();// 绑定端口，开始接收进来的连接
        log.info(port + "服务端启动成功...");
        // 监听服务器关闭监听
        f.channel().closeFuture().addListener((future) -> {
            bossGroup.shutdownGracefully(); //关闭EventLoopGroup，释放掉所有资源包括创建的线程
        });
    }


//    @Override
//    public void run(String... args) throws Exception {
//        runHandlerAsServer(port,null);
//    }
}

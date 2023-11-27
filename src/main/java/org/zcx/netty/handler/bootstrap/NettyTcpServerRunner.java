package org.zcx.netty.handler.bootstrap;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zcx.netty.handler.DynamicHandler;

@Component
public class NettyTcpServerRunner {
    private final Logger log = LoggerFactory.getLogger(NettyTcpServerRunner.class);
//    @Resource
//    private ServerGatewayHandler gatewayHandler;

    private int port = 18021;

    public void runHandlerAsServer(int port, DynamicHandler handler) throws Exception {
        //NioEventLoopGroup是用来处理IO操作的多线程事件循环器
        EventLoopGroup bossGroup = new NioEventLoopGroup();  // 用来接收进来的连接
        EventLoopGroup workerGroup = new NioEventLoopGroup();// 用来处理已经被接收的连接
        ServerBootstrap server = new ServerBootstrap();//是一个启动NIO服务的辅助启动类
        server.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)  // 这里告诉Channel如何接收新的连接
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
//                        ch.pipeline().addLast(new TestHandler());
                        // 自定义处理类
//                        ch.pipeline().addLast(gatewayHandler);
                        ch.pipeline().addLast(handler.initHandlers());
                    }
                });
        server.option(ChannelOption.SO_BACKLOG, 128);
        server.childOption(ChannelOption.SO_KEEPALIVE, true);
        ChannelFuture f = server.bind(port).sync();// 绑定端口，开始接收进来的连接
        log.info(port + "服务端启动成功...");
        // 监听服务器关闭监听
        f.channel().closeFuture().addListener((future) -> {
            bossGroup.shutdownGracefully(); //关闭EventLoopGroup，释放掉所有资源包括创建的线程
            workerGroup.shutdownGracefully();
        });
    }

//    @Override
//    public void run(String... args) throws Exception {
//        this.start();
//    }
}

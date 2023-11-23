package org.zcx.netty.bootstrap;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zcx.netty.common.DynamicHandler;

@Component
public class NettyClientRunner {
    private final Logger log = LoggerFactory.getLogger(NettyClientRunner.class);

    public void runHandlerAsClient(String ip, int port, DynamicHandler handler) {
        new Bootstrap().group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(handler.initHandlers());
                    }
                })
                .connect(ip, port)
                .addListener((future) -> {
                    if (future.isSuccess()) {
                        log.info("connect {}:{} success.", ip, port);
                    } else {
                        log.error("connect {}:{} fail", ip, port, future.cause());
                    }
                });

    }
}

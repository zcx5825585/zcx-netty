package org.zcx.netty.bootstrap;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zcx.netty.common.ClientHandler;
import org.zcx.netty.common.DynamicHandler;
import org.zcx.netty.common.exception.HandlerException;

@Component
public class NettyTcpClientRunner {
    private final Logger log = LoggerFactory.getLogger(NettyTcpClientRunner.class);

    public void runHandlerAsClient(DynamicHandler handler) {
        if (!(handler instanceof ClientHandler)) {
            throw new HandlerException("不是客户端handler");
        }
        String ip = ((ClientHandler) handler).getHost();
        Integer port = ((ClientHandler) handler).getPort();
        if (ip == null || ip.length() < 1 || port == null || port == 0) {
            throw new HandlerException("服务端ip或端口信息错误");
        }
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
                        throw new HandlerException("连接服务端 "+ip+":"+port+" 失败");
                    }
                });

    }
}

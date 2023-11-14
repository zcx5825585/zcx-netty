package org.zcx.netty.handler.impl;

import io.netty.channel.*;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.zcx.netty.handler.DynamicHandler;
import org.zcx.netty.handler.WritableHandler;
import org.zcx.netty.utils.SpringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("wsHandler")
@ChannelHandler.Sharable
public class WsHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> implements DynamicHandler, WritableHandler {
    private final Log logger = LogFactory.getLog(this.getClass());

    private Map<String, ChannelHandlerContext> channelMap = new HashMap<>();

    @Override
    public void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        logger.info("服务器接受消息：" + msg.text());

    }

    @Override
    public List<String> getChannelList() {
        return new ArrayList<>(channelMap.keySet());
    }

    @Override
    public void sendMsg(String channelId, String msg) {
        ChannelHandlerContext ctx = channelMap.get(channelId);
        ctx.channel().writeAndFlush(new TextWebSocketFrame(msg));
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        String channelId = ctx.channel().id().asShortText();
        channelMap.put(channelId, ctx);
        logger.info("【服务器】用户" + channelId + "已连接！");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        String channelId = ctx.channel().id().asShortText();
        channelMap.remove(channelId);
        logger.info("【服务器】用户" + channelId + "已断开连接");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.info("发生异常：" + cause.getMessage());
        ctx.close();
    }

    @Override
    public ChannelHandler initHandlers() {
        return new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(new HttpServerCodec());
                ch.pipeline().addLast(new HttpObjectAggregator(512 * 1024));
                ch.pipeline().addLast(new WebSocketServerProtocolHandler("/ws"));
                ch.pipeline().addLast(SpringUtils.getBean("wsHandler", DynamicHandler.class));
            }
        };
    }
}

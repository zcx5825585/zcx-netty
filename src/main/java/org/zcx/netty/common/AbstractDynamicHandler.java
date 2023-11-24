package org.zcx.netty.common;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.beans.Introspector;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractDynamicHandler<I> extends SimpleChannelInboundHandler<I> implements DynamicHandler {

    protected Map<String, ChannelHandlerContext> channelMap = new HashMap<>();

    public abstract ChannelHandler[] initHandlers();

    public void channelActive0(ChannelHandlerContext ctx) throws Exception {
    }

    public void channelInactive0(ChannelHandlerContext ctx) throws Exception {
    }

    protected ChannelHandlerContext getChannel(String channelId) {
        return channelMap.get(channelId);
    }

    @Override
    public void disconnect(String channelId) throws Exception {
        ChannelHandlerContext ctx = channelMap.get(channelId);
        if (ctx != null) {
            channelInactive(ctx);
        }

    }

    @Override
    public String getHandlerName() {
        return Introspector.decapitalize(this.getClass().getSimpleName());
    }

    @Override
    public List<String> getChannelList() {
        return channelMap.values().stream().map(ChannelHandlerContext::channel).map(Channel::toString).collect(Collectors.toList());
    }

    @Override
    public void sendMsg(String channelId, Object msg) {
        getChannel(channelId).channel().write(msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String channelId = ctx.channel().id().asShortText();
        channelMap.put(channelId, ctx);
        channelActive0(ctx);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String channelId = ctx.channel().id().asShortText();
        channelMap.remove(channelId);
        channelInactive0(ctx);
        ctx.close();
    }
}

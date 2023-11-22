package org.zcx.netty.common;

import io.netty.channel.ChannelHandler;

import java.util.List;

public interface DynamicHandler extends ChannelHandler {

    public String getHandlerName();

    public ChannelHandler[] initHandlers();

    public List<String> getChannelList();

    public void sendMsg(String channelId, Object msg);

    public void disconnect(String channelId) throws Exception;

}

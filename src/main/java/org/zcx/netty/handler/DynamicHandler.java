package org.zcx.netty.handler;

import io.netty.channel.ChannelHandler;

public interface DynamicHandler extends ChannelHandler {

    public ChannelHandler initHandlers();
}

package org.zcx.netty.handler;

import java.util.List;

public interface WritableHandler {
    public List<String> getChannelList();
    public void sendMsg(String channelId, String msg);
}

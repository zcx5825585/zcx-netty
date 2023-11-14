package org.zcx.netty.bean;

import io.netty.channel.ChannelHandler;
import org.springframework.stereotype.Component;
import org.zcx.netty.handler.DynamicHandler;
import org.zcx.netty.handler.WritableHandler;
import org.zcx.netty.utils.BeanRegisterUtils;
import org.zcx.netty.utils.SpringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class HandlerManager {

    @Resource
    private Map<String, DynamicHandler> handlerMap;

    public ChannelHandler getInitHandlers(String handlerName) {
        if (!handlerMap.containsKey(handlerName)) {
            return null;
        }
        return handlerMap.get(handlerName).initHandlers();
    }

    public ChannelHandler getHandler(String handlerName) {
        ChannelHandler bean = SpringUtils.getBean(handlerName, ChannelHandler.class);
        return bean;
    }

    public WritableHandler getWritableHandler(String handlerName) {
        ChannelHandler handler = SpringUtils.getBean(handlerName, ChannelHandler.class);
        if (handler instanceof WritableHandler) {
            WritableHandler writableHandler = (WritableHandler) handler;
            return writableHandler;
        }
        return null;
    }
    public List<String> getAllHandlerNames() {
        return new ArrayList<>(handlerMap.keySet());
    }

    public boolean registerHandler(String handlerName) throws Exception {
        if (BeanRegisterUtils.registerBean(handlerName, false)) {
            DynamicHandler bean = SpringUtils.getBean(handlerName, DynamicHandler.class);
            handlerMap.put(handlerName, bean);
            return true;
        }
        return false;
    }

}

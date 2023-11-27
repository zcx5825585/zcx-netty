package org.zcx.netty.handler.configurableHandler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.zcx.netty.common.DynamicHandler;
import org.zcx.netty.handler.abstractHandler.AbstractWsHandler;
import org.zcx.netty.common.bean.BeanParam;
import org.zcx.netty.common.bean.ConfigurableBean;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component("wsHandler")
@ChannelHandler.Sharable
public class WsHandler extends AbstractWsHandler implements ConfigurableBean, DynamicHandler {
    private final Log log = LogFactory.getLog(this.getClass());

    private String websocketPath;

    @Override
    public String getWebsocketPath() {
        return websocketPath;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        log.info(getHandlerName() + "接收到websocket消息 " + "\n" + msg.toString());
    }

    @Override
    public void setBeanName(String beanName) {
        this.handlerName = beanName;
    }

    @Override
    public void config(Map<String, Object> param) {
        this.websocketPath = (String) param.get("websocketPath");
    }

    @Override
    public List<BeanParam> getParamList() {
        return Arrays.asList(
                new BeanParam("websocketPath","连接url", String.class)
        );
    }
}

package dynamicBean.tcpServerHandler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.zcx.netty.handler.abstractHandler.AbstractWsHandler;

@Component("ws2Handler")
@ChannelHandler.Sharable
public class Ws2Handler extends AbstractWsHandler {
    private final Log log = LogFactory.getLog(this.getClass());
    @Override
    public String getWebsocketPath() {
        return "/ws2";
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        log.info(getHandlerName() +"接收到websocket消息 " +  "\n" + msg.toString());
    }
}

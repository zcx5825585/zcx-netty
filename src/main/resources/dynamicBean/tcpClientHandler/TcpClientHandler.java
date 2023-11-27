package dynamicBean.tcpClientHandler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.zcx.netty.common.AbstractDynamicHandler;
import org.zcx.netty.common.ClientHandler;
import org.zcx.netty.common.DynamicHandler;
import org.zcx.netty.common.HandlerManager;
import org.zcx.netty.common.utils.SpringUtils;

import java.io.IOException;

@Component
@ChannelHandler.Sharable
public class TcpClientHandler extends AbstractDynamicHandler<String> implements DynamicHandler, ClientHandler {
    private final Log log = LogFactory.getLog(this.getClass());

    @Override
    public String getHost() {
        return "127.0.0.1";
    }

    @Override
    public Integer getPort() {
        return 18021;
    }

    public ChannelHandler[] initHandlers() {
        return new ChannelHandler[]{
                new StringEncoder(CharsetUtil.UTF_8),
                new StringDecoder(CharsetUtil.UTF_8),
                HandlerManager.getDynamicHandler(getHandlerName())
        };
    }

    /**
     * 从服务端收到新的数据时，这个方法会在收到消息时被调用
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception, IOException {
        if (msg == null) {
            return;
        }
        String channelId = ctx.channel().id().asShortText();
        log.info(String.format("%s[%s]接收到tcp消息\n%s", getHandlerName(), channelId, msg.toString()));
    }

    @Override
    public void sendMsg(String channelId, Object msg) {
        String massage = (String) msg;
        getChannel(channelId).writeAndFlush(massage);
    }
}
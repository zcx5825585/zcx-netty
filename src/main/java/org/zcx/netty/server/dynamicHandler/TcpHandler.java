package org.zcx.netty.server.dynamicHandler;


import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.zcx.netty.common.AbstractDynamicHandler;
import org.zcx.netty.common.DynamicHandler;
import org.zcx.netty.common.utils.SpringUtils;

@Component("tcpHandler")
@ChannelHandler.Sharable
public class TcpHandler extends AbstractDynamicHandler<String> implements DynamicHandler {

    private final Log log = LogFactory.getLog(this.getClass());

    public ChannelHandler[] initHandlers() {
        return new ChannelHandler[]{
                new StringEncoder(CharsetUtil.UTF_8),
                new StringDecoder(CharsetUtil.UTF_8),
                SpringUtils.getBean(getHandlerName(), DynamicHandler.class)
        };
    }

    private int count = 0;

    //收到数据时调用
    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        count++;
        try {
            String channelId = ctx.channel().id().asShortText();
            log.info(String.format("%s[%s]接收到tcp消息\n%s", getHandlerName(), channelId, msg.toString()));
            ctx.writeAndFlush("tcp connect " + count);
        } catch (Exception e) {
            log.error(e);
        } finally {
            // 抛弃收到的数据
            ReferenceCountUtil.release(msg);
        }
    }


    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void sendMsg(String channelId, Object msg) {
        getChannel(channelId).channel().writeAndFlush(msg);
    }

    /*
     * 建立连接时，返回消息
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

    }

}

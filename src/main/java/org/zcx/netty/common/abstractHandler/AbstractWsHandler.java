package org.zcx.netty.common.abstractHandler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zcx.netty.common.AbstractDynamicHandler;
import org.zcx.netty.common.DynamicHandler;
import org.zcx.netty.common.utils.SpringUtils;

@ChannelHandler.Sharable
public abstract class AbstractWsHandler extends AbstractDynamicHandler<TextWebSocketFrame> implements DynamicHandler {
    private final Log log = LogFactory.getLog(this.getClass());

    public abstract String getWebsocketPath();

    public abstract void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg);

    @Override
    public ChannelHandler[] initHandlers() {
        return new ChannelHandler[]{
                new HttpServerCodec(),
                new HttpObjectAggregator(512 * 1024),
                new WebSocketServerProtocolHandler(getWebsocketPath()),
                SpringUtils.getBean(getHandlerName(), DynamicHandler.class)
        };
    }


    @Override
    public void sendMsg(String channelId, Object msg) {
        ChannelHandlerContext ctx = getChannel(channelId);
        ctx.channel().writeAndFlush(new TextWebSocketFrame((String) msg));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        String channelId = ctx.channel().id().asShortText();
        log.info("【服务器】用户" + channelId + "已连接！");
    }


    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        String channelId = ctx.channel().id().asShortText();
        log.info("【服务器】用户" + channelId + "已断开连接");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("发生异常：" + cause.getMessage());
        ctx.close();
    }

}

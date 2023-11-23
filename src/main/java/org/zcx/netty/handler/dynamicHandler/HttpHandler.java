package org.zcx.netty.handler.dynamicHandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.zcx.netty.common.AbstractDynamicHandler;
import org.zcx.netty.common.DynamicHandler;
import org.zcx.netty.common.HandlerManager;
import org.zcx.netty.common.utils.RequestHelper;

import java.nio.charset.StandardCharsets;

@Component("httpHandler")
@ChannelHandler.Sharable
public class HttpHandler extends AbstractDynamicHandler<FullHttpRequest> implements DynamicHandler {

    private final Log log = LogFactory.getLog(this.getClass());

    public ChannelHandler[] initHandlers() {
        return new ChannelHandler[]{
                new HttpServerCodec(),
                new HttpObjectAggregator(512 * 1024),
                HandlerManager.getDynamicHandler(getHandlerName())
        };
    }

    private int count = 0;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        count++;
        log.info(getHandlerName() + "接收到http消息 " + "\n" + request.toString());
        String uri = request.uri();
        String body = getBody(request);
        RequestHelper.sendTxt(ctx, "http connect " + count);
    }

    public static String getBody(FullHttpRequest request) {
        ByteBuf buf = request.content();
        byte[] bytes = ByteBufUtil.getBytes(buf);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("http Active ");
        super.channelActive(ctx);
    }
}

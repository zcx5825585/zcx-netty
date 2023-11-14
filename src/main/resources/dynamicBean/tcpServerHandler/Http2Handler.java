package dynamicBean.tcpServerHandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zcx.netty.common.AbstractDynamicHandler;
import org.zcx.netty.common.DynamicHandler;
import org.zcx.netty.common.utils.RequestHelper;
import org.zcx.netty.common.utils.SpringUtils;

import java.nio.charset.StandardCharsets;

@ChannelHandler.Sharable
public class Http2Handler extends AbstractDynamicHandler<FullHttpRequest> implements DynamicHandler {

    private final Log log = LogFactory.getLog(this.getClass());

    @Override
    public ChannelHandler[] initHandlers() {
        return new ChannelHandler[]{
                new HttpServerCodec(),
                new HttpObjectAggregator(512 * 1024),
                SpringUtils.getBean(getHandlerName(), DynamicHandler.class)
        };
    }

    private int count = 0;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        count++;
        String uri = request.uri();
        String method = request.method().name();
        String body = getBody(request);
        log.info("接收到http消息 \nhandler：" + getHandlerName() + "\n" + request.toString());
        RequestHelper.sendTxt(ctx, "http2 connect " + count);
    }

    public static String getBody(FullHttpRequest request) {
        ByteBuf buf = request.content();
        byte[] bytes = ByteBufUtil.getBytes(buf);
        return new String(bytes, StandardCharsets.UTF_8);
    }

}

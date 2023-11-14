package dynamicBean;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zcx.netty.handler.DynamicHandler;
import org.zcx.netty.utils.RequestHelper;
import org.zcx.netty.utils.SpringUtils;

import java.nio.charset.StandardCharsets;

@ChannelHandler.Sharable
public class Http3Handler extends SimpleChannelInboundHandler<FullHttpRequest> implements DynamicHandler {

    private final Log logger = LogFactory.getLog(this.getClass());

    public ChannelHandler initHandlers() {
        return new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(new HttpServerCodec());
                ch.pipeline().addLast(new HttpObjectAggregator(512 * 1024));
                ch.pipeline().addLast(SpringUtils.getBean("http2Handler", DynamicHandler.class));
            }
        };
    }


    private int count = 0;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        count++;
        logger.info("http3 connect " + count);
        String uri = request.uri();
        String body = getBody(request);
        RequestHelper.sendTxt(ctx, "http3 connect " + count);
    }

    public static String getBody(FullHttpRequest request) {
        ByteBuf buf = request.content();
        byte[] bytes = ByteBufUtil.getBytes(buf);
        return new String(bytes, StandardCharsets.UTF_8);
    }

}

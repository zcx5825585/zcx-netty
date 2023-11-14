package org.zcx.netty.handler.impl;


import io.netty.channel.*;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.zcx.netty.handler.DynamicHandler;
import org.zcx.netty.utils.SpringUtils;

@Component("tcpHandler")
@ChannelHandler.Sharable
public class TcpHandler extends SimpleChannelInboundHandler<String> implements DynamicHandler {

    private final Log logger = LogFactory.getLog(this.getClass());

    public ChannelHandler initHandlers() {
        return new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
                ch.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8));
                ch.pipeline().addLast(SpringUtils.getBean("tcpHandler", DynamicHandler.class));
            }
        };
    }

    private int count = 0;

    //收到数据时调用
    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        count++;
        logger.info("tcp connect " + count);
        try {
            logger.info("服务端接收的消息 : " + msg);

            ctx.writeAndFlush("tcp connect " + count);

        } catch (Exception e) {
            logger.error(e);
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

    /*
     * 建立连接时，返回消息
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("连接的客户端地址:" + ctx.channel().remoteAddress());
        logger.info("连接的客户端ID:" + ctx.channel().id());
        super.channelActive(ctx);

    }

}

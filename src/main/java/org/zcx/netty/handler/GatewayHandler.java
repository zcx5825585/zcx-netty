package org.zcx.netty.handler;


import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.zcx.netty.bean.HandlerManager;

import javax.annotation.Resource;

@Component
@ChannelHandler.Sharable
public class GatewayHandler extends ChannelInboundHandlerAdapter {

    @Resource
    private HandlerManager handlerManager;

    private final Log logger = LogFactory.getLog(this.getClass());

    /*
     * 建立连接时，返回消息
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("连接的客户端地址:" + ctx.channel().remoteAddress());
        logger.info("连接的客户端ID:" + ctx.channel().id());
        super.channelActive(ctx);

        String remoteIpAddr = ctx.channel().remoteAddress().toString();
        remoteIpAddr = remoteIpAddr.replace("/", "");
        remoteIpAddr = remoteIpAddr.replace(":", "-");

        setHandlers(ctx);
        super.channelActive(ctx);
    }

    private int count = 0;

    public void setHandlers(ChannelHandlerContext ctx) {
        //根据key获取handler
        String key ="wsHandler";
//        if (count++ % 2 == 0) {
//            key = "httpHandler";
//        } else {
//            key = "http2Handler";
//        }
        ChannelHandler handlers = handlerManager.getInitHandlers(key);
        if (handlers != null) {
            ctx.pipeline().addLast(handlers);
        } else {
            ctx.fireChannelInactive();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        String remoteIpAddr = ctx.channel().remoteAddress().toString();
        remoteIpAddr = remoteIpAddr.replace("/", "");
        remoteIpAddr = remoteIpAddr.replace(":", "-");

        ctx.channel().close();
        logger.warn("连接已断开。");
        super.channelInactive(ctx);
    }
}

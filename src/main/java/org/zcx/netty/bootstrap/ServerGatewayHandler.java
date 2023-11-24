package org.zcx.netty.bootstrap;


import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.zcx.netty.common.DynamicHandler;
import org.zcx.netty.common.HandlerManager;
import org.zcx.netty.web.dao.HandlerInfoDao;
import org.zcx.netty.web.entity.HandlerInfo;

import javax.annotation.Resource;

@Component
@ChannelHandler.Sharable
public class ServerGatewayHandler extends ChannelInboundHandlerAdapter {

    @Resource
    private HandlerInfoDao handlerDao;
    @Resource
    private HandlerManager handlerManager;

    private final Log log = LogFactory.getLog(this.getClass());

    /*
     * 建立连接时，返回消息
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("连接的客户端地址:" + ctx.channel().remoteAddress());
        log.info("连接的客户端ID:" + ctx.channel().id());
        //建立连接时设置handler
//        setHandlers(ctx);
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //接收到出示信息 获取绑定的handler 服务升级
        if (msg != null){//todo 绑定信息获取 多条件匹配
            //todo 获取相应handler
            HandlerInfo handlerInfo = handlerDao.getBySn("tcpHandler");
            DynamicHandler dynamicHandler = handlerManager.getDynamicHandler(handlerInfo.getHandlerName());
            log.info("服务升级:" + ctx.channel().id());
            ctx.pipeline().addLast(dynamicHandler.initHandlers());
            ctx.pipeline().remove(ServerGatewayHandler.class);//移除网关
            ctx.fireChannelActive();//调用连接建立钩子
            ctx.fireChannelRead(msg);//继续处理本条消息
        }
    }

    private int count = 0;

    public void setHandlers(ChannelHandlerContext ctx) {
        //根据key获取handler
        HandlerInfo handlerInfo;
        handlerInfo = handlerDao.getByName("tcpHandler");
//        handlerInfo = handlerDao.getByName("ws2Handler");

//        if (count++ % 2 == 0) {
//            handlerInfo = handlerDao.getByName("wsHandler");
//        } else {
//            handlerInfo = handlerDao.getByName("ws2Handler");
//        }
        DynamicHandler dynamicHandler = handlerManager.getDynamicHandler(handlerInfo.getHandlerName());
        ctx.pipeline().addLast(dynamicHandler.initHandlers());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        ctx.channel().close();
        log.warn("连接已断开。");
        super.channelInactive(ctx);
    }
}

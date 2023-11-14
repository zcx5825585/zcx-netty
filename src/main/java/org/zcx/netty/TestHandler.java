package org.zcx.netty;


import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.zcx.netty.server.dynamicHandler.HttpHandler;

import javax.annotation.Resource;

@Component
@ChannelHandler.Sharable
public class TestHandler extends ChannelInboundHandlerAdapter {

    private final Log log = LogFactory.getLog(this.getClass());


    @Resource
    private HttpHandler httpHandler;

    private int count = 0;

    /*
     * 建立连接时，返回消息
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("连接的客户端地址:" + ctx.channel().remoteAddress());
        log.info("连接的客户端ID:" + ctx.channel().id());
        super.channelActive(ctx);

        String remoteIpAddr = ctx.channel().remoteAddress().toString();
        remoteIpAddr = remoteIpAddr.replace("/", "");
        remoteIpAddr = remoteIpAddr.replace(":", "-");
        ChannelPipeline pipeline = ctx.pipeline();
//        if (count % 2 == 0) {
        ctx.pipeline().addLast(

//                new ChannelInboundHandlerAdapter() {
//                    @Override
//                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
////                            System.out.println(msg);
//                        ctx.fireChannelRead(msg);
//                    }
//                },
//                new ByteToMessageDecoder() {
//                    @Override
//                    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
////                            System.out.println(byteBuf);
//                        list.add("test");
//                        ByteBuf frame = byteBuf.retainedDuplicate();
//                        list.add(frame);
//                        byteBuf.skipBytes(byteBuf.readableBytes());
//                    }
//                },
                new HttpServerCodec(),
                new HttpObjectAggregator(512 * 1024),
                new ChannelInboundHandlerAdapter() {
                    @Override
                    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
                        for (ChannelHandler handler : ctx.pipeline().toMap().values()) {
                            System.out.println(handler.getClass());
                        }
                    }

                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        for (ChannelHandler handler : ctx.pipeline().toMap().values()) {
                            System.out.println(handler.getClass());
                        }
                        ctx.fireChannelRead(msg);
                    }

                },
                new HttpHandler()
        );
//        } else {
//            ctx.pipeline().addLast(modbusHandler);
//        }
//        count++;

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        String remoteIpAddr = ctx.channel().remoteAddress().toString();
        remoteIpAddr = remoteIpAddr.replace("/", "");
        remoteIpAddr = remoteIpAddr.replace(":", "-");

        ctx.channel().close();
        log.warn("连接已断开。");
    }
}

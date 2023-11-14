package org.zcx.netty.handler;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.zcx.netty.handler.impl.HttpHandler;
import org.zcx.netty.handler.impl.TcpHandler;

import javax.annotation.Resource;
import java.util.List;

@Component
@ChannelHandler.Sharable
public class TestHandler extends ChannelInboundHandlerAdapter {

    private final Log logger = LogFactory.getLog(this.getClass());

    @Resource
    private TcpHandler tcpHandler;

    @Resource
    private HttpHandler httpHandler;

    private int count = 0;

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
        ChannelPipeline pipeline = ctx.pipeline();
//        if (count % 2 == 0) {
        ctx.pipeline().addLast(
                new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                            System.out.println(msg);
                        ctx.fireChannelRead(msg);
                    }
                }
                , new ByteToMessageDecoder() {
                    @Override
                    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
//                            System.out.println(byteBuf);
                        list.add("test");
                        ByteBuf frame = byteBuf.retainedDuplicate();
                        list.add(frame);
                        byteBuf.skipBytes(byteBuf.readableBytes());
                    }
                }
                , new HttpServerCodec()
                , new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        System.out.println(msg.getClass());
                        ctx.fireChannelRead(msg);
                    }
                }
                , new HttpObjectAggregator(512 * 1024)
                , httpHandler
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
        logger.warn("连接已断开。");
    }
}

package org.zcx.netty.coap;


import cn.hutool.core.util.HexUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.zcx.netty.coap.entity.CoapMessage;
import org.zcx.netty.handler.DynamicHandler;
import org.zcx.netty.handler.HandlerManager;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

@Component
public class MyCoapHandler extends SimpleChannelInboundHandler<CoapMessage> implements DynamicHandler {
    private final Log log = LogFactory.getLog(this.getClass());
    private ChannelHandlerContext ctx;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, CoapMessage coapMessage)
            throws Exception {

        try {

            CoapMessage ack = coapMessage.createAck(69);
            log.debug(String.format("接收到payload：%s", coapMessage.getPayload()));
            ack.setPayload("My block ack!");

            log.debug(String.format("生成数据：%s", ack.toString()));

            ctx.writeAndFlush(ack);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public String getHandlerName() {
        return "udpHandler";
    }

    @Override
    public ChannelHandler[] initHandlers() {
        return new ChannelHandler[]{
                HandlerManager.getDynamicHandler(getHandlerName())
        };
    }

    @Override
    public List<String> getChannelList() {
        return Collections.singletonList(ctx.channel().id().asShortText());
    }

    public void sendMsg0(String host, Integer port, String msg) {
        InetSocketAddress socketAddress = new InetSocketAddress(host, port);
        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(HexUtil.decodeHex("11")), socketAddress));
    }

    @Override
    public void sendMsg(String channelId, Object msg) {
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 5683);
        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(HexUtil.decodeHex("11")), socketAddress));
    }

    @Override
    public void disconnect(String channelId) throws Exception {

    }
}

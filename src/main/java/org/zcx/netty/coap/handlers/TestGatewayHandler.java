package org.zcx.netty.coap.handlers;


import cn.hutool.core.util.HexUtil;
import io.netty.buffer.ByteBuf;
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
public class TestGatewayHandler extends SimpleChannelInboundHandler<DatagramPacket> implements DynamicHandler {
    private final Log log = LogFactory.getLog(this.getClass());
    private ChannelHandlerContext ctx;
    private CoapMessageDecoder decoder = new CoapMessageDecoder();
    private CoapMessageEncoder encoder = new CoapMessageEncoder();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        super.channelActive(ctx);
    }

    private InetSocketAddress sender;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket packet)
            throws Exception {

        try {
            ByteBuf in = packet.copy().content().copy();
            InetSocketAddress sender = packet.sender();
            CoapMessage inMsg = decoder.decode(in.copy());
            if (sender.getPort() != 5683) {
                log.debug(String.format("请求：%s", inMsg));
                CoapMessage ack = inMsg.createAck(69);
                log.debug(String.format("生成：%s", ack));
                this.sender = sender;
                InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 5683);
                ctx.writeAndFlush(new DatagramPacket(in.copy(), socketAddress));
            } else {
                log.debug(String.format("响应：%s", inMsg));
                ctx.writeAndFlush(new DatagramPacket(in.copy(), this.sender));
                this.sender = null;
            }
            // 下面进行业务代码处理
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String bytesToBinaryString(byte[] bytes) {
        StringBuilder binaryString = new StringBuilder();
        for (byte b : bytes) {
            // 将每个字节转换为8位二进制字符串，并添加到StringBuilder中
            binaryString.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        return binaryString.toString();
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

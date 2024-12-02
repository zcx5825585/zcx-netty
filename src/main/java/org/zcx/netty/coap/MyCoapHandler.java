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
import org.zcx.netty.coap.common.CoapMessageCode;
import org.zcx.netty.coap.common.CoapMessageType;
import org.zcx.netty.coap.entity.CoapMessage;
import org.zcx.netty.coap.entity.CoapMessageOptions;
import org.zcx.netty.coap.utils.BytesUtils;
import org.zcx.netty.handler.DynamicHandler;
import org.zcx.netty.handler.HandlerManager;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
            log.info(String.format("接收到payload：%s", coapMessage.getPayload()));

            CoapMessage ack = coapMessage.createAck(CoapMessageCode.CONTENT_205);
            ack.setPayload("My ack !");

            String body = "{\"ok\":1}";
            byte[] one = body.getBytes(StandardCharsets.UTF_8);
            int time = 2048/8;
            byte[] payload = new byte[one.length * time];
            for (int i = 0; i < time; i++) {
                for (int j = 0; j < one.length; j++) {
                    payload[j + i * one.length] = one[j];
                }
            }
            ack.setPayload(new String(payload));
            log.info(String.format("生成响应数据：%s", ack.toString()));

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
        CoapMessage ack = new CoapMessage();
        byte[] token =  BytesUtils.longToBytes( new Random(System.currentTimeMillis()).nextLong());
        ack.setVersion(1);
        ack.setMessageType(CoapMessageType.ACK);
        ack.setTokenLength(token.length);
        ack.setMessageCode(2);
        ack.setMessageID(30720);
        ack.setToken(token);
        ack.setSender(socketAddress);
        CoapMessageOptions options = new CoapMessageOptions();
        ack.setOptions(options);
        ack.setPayload("send test");
        ctx.writeAndFlush(ack);
    }

    @Override
    public void disconnect(String channelId) throws Exception {

    }
}

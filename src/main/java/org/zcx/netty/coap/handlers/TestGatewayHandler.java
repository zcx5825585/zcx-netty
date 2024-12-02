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
import org.zcx.netty.coap.common.CoapMessageCode;
import org.zcx.netty.coap.common.CoapMessageType;
import org.zcx.netty.coap.common.CoapOptionType;
import org.zcx.netty.coap.entity.CoapBlock;
import org.zcx.netty.coap.entity.CoapMessage;
import org.zcx.netty.coap.entity.CoapMessageOptions;
import org.zcx.netty.coap.utils.BytesUtils;
import org.zcx.netty.handler.DynamicHandler;
import org.zcx.netty.handler.HandlerManager;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class TestGatewayHandler extends SimpleChannelInboundHandler<DatagramPacket> implements DynamicHandler {
    private final Log log = LogFactory.getLog(this.getClass());
    private ChannelHandlerContext ctx;
    private CoapMessageDecoder decoder = new CoapMessageDecoder();
    private CoapMessageEncoder encoder = new CoapMessageEncoder();
    private ServerBlock2Handler block2Handler = new ServerBlock2Handler();

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
                CoapMessageOptions options = inMsg.getOptions();
                log.debug(String.format("请求：%s", inMsg));
                this.sender = sender;
                //观察模式
                // 客户端发送请求 options包含observe ,值为任意
                // 服务端响应    options包含observe 值为当前序号（版本？），在资源中维护 payload返回说明信息（无固定格式）
                // 服务端发送通知 options包含observe 值为当前序号（版本？），在资源中维护 payload为资源的值
                //请求：CoapMessage{version=1, messageType=0, tokenLength=8, messageCode=1, messageID=11388, token='bf765867e3c032bf', options=6:null;11:hello;, payload=''}
                //响应：CoapMessage{version=1, messageType=2, tokenLength=8, messageCode=69, messageID=11388, token='bf765867e3c032bf', options=6:4;12:null;, payload='/127.0.0.1:18020subscription successful,resource:/hello'}
                //响应：CoapMessage{version=1, messageType=1, tokenLength=8, messageCode=69, messageID=41076, token='bf765867e3c032bf', options=6:5;12:null;, payload='{"value":"4"}'}
                //todo 非block请求可以处理，block如何处理？
                CoapMessage ack = inMsg.createAck(CoapMessageCode.CONTENT_205);
                CoapMessageOptions ackOptions = ack.getOptions();
                ackOptions.put(6,new byte[1]);
                ackOptions.putEmpty(12);
                ack.setPayload("/127.0.0.1:18020subscription successful,resource:/hello");
                log.debug(String.format("生成：%s", ack));
                ctx.writeAndFlush(new DatagramPacket(encoder.doEncode(ack), this.sender));
                new Thread(() -> {
                    try {
                        int i = 0;
                        while (true) {
                            Thread.sleep(1000); // 等待5秒
                            try {
                                ack.setPayload("{\"value\":\""+i+"\"}");
                                ack.setMessageID(ack.getMessageID()+1);
                                ack.setMessageType(CoapMessageType.NON);
                                ack.getOptions().putObject(6,i);
                                log.debug(String.format("生成：%s", ack));
                                ctx.writeAndFlush(new DatagramPacket(encoder.doEncode(ack), this.sender));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            i++;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
                InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 5683);
                ctx.writeAndFlush(new DatagramPacket(in.copy(), socketAddress));

            } else {
                CoapMessageOptions options = inMsg.getOptions();
                log.debug(String.format("响应：%s", inMsg));
                ctx.writeAndFlush(new DatagramPacket(in.copy(), this.sender));
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

package org.zcx.netty.handler.dynamicHandler;


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
import org.zcx.netty.handler.DynamicHandler;
import org.zcx.netty.handler.HandlerManager;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

@Component
public class UdpHandler extends SimpleChannelInboundHandler<DatagramPacket> implements DynamicHandler {
    private final Log log = LogFactory.getLog(this.getClass());
    private ChannelHandlerContext ctx;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket)
            throws Exception {
        log.info(channelHandlerContext.channel().id().asShortText());
        InetSocketAddress sender = datagramPacket.sender();
        String ip = sender.getAddress().getHostAddress();
        ByteBuf buf = datagramPacket.copy().content();
        try {
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            String dataStr = HexUtil.encodeHexStr(data);
            log.info(String.format("收到IP：%s,发送的数据：%s", ip, dataStr));
            // 下面进行业务代码处理
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
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 10000);
        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(HexUtil.decodeHex("11")), socketAddress));
    }

    @Override
    public void disconnect(String channelId) throws Exception {

    }
}

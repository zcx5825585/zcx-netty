package org.zcx.netty.coap.handlers;

import cn.hutool.core.util.HexUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zcx.netty.coap.entity.CoapMessage;
import org.zcx.netty.coap.entity.CoapMessageOptions;

public class CoapMessageDecoder extends SimpleChannelInboundHandler<DatagramPacket> {
    private final Log log = LogFactory.getLog(this.getClass());

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket datagramPacket) throws Exception {
        ByteBuf buf = datagramPacket.copy().content();
        CoapMessage coapMessage = decode(buf);
        coapMessage.setSender(datagramPacket.sender());
        log.debug(String.format("收到数据：%s", coapMessage.toString()));
        ctx.fireChannelRead(coapMessage);
    }

    protected CoapMessage decode(ByteBuf in) throws Exception {
        CoapMessage coapMessage = new CoapMessage();
        ByteBuf buf = in;
//        int firstByte = buf.readByte() & 0xFF;
//        System.out.println((firstByte / (int) Math.pow(2, 6) % (int) Math.pow(2, 2)));
//        System.out.println((firstByte / (int) Math.pow(2, 4) % (int) Math.pow(2, 2)));
//        System.out.println((firstByte % (int) Math.pow(2, 4)));
//        firstByte = buf.readByte() & 0xFF;
//        System.out.println(firstByte);
//        int a = 0;
//        a = (int) (Math.pow(2, 8) * (buf.readByte() & 0xFF));
//        a = a + buf.readByte() & 0xFF;
//        System.out.println(a);
//        buf =in.copy();
        int encodedHeader = buf.readInt();//读取32bit
        int version = (encodedHeader >>> 30) & 0x03;//读取2bit
        int messageType = (encodedHeader >>> 28) & 0x03;//读取2bit
        int tokenLength = (encodedHeader >>> 24) & 0x0F;//读取4bit
        int messageCode = (encodedHeader >>> 16) & 0xFF;//读取8bit
        int messageID = (encodedHeader) & 0xFFFF;//读取16bit
        coapMessage.setVersion(version);
        coapMessage.setMessageType(messageType);
        coapMessage.setTokenLength(tokenLength);
        coapMessage.setMessageCode(messageCode);
        coapMessage.setMessageID(messageID);

        byte[] token = new byte[tokenLength];
        buf.readBytes(token);
        String tokenStr = HexUtil.encodeHexStr(token);
        coapMessage.setToken(token);

        if (buf.readableBytes() > 0) {
            CoapMessageOptions options = readOptions(buf);
            coapMessage.setOptions(options);
        }
        buf.discardReadBytes();
        String payload = buf.toString(CharsetUtil.UTF_8);
        coapMessage.setPayload(payload);

        buf.skipBytes(buf.readableBytes());

        return coapMessage;
    }

    private CoapMessageOptions readOptions(ByteBuf buffer) {
        CoapMessageOptions options = new CoapMessageOptions();

        //Decode the options
        int previousOptionNumber = 0;
        int firstByte = buffer.readByte() & 0xFF;

        while (firstByte != 0xFF && buffer.readableBytes() >= 0) {
            int optionDelta = (firstByte & 0xF0) >>> 4;
            int optionLength = firstByte & 0x0F;
//            System.out.println(optionDelta);
//            System.out.println(optionLength);

            if (optionDelta == 13) {
                optionDelta += buffer.readByte() & 0xFF;
            } else if (optionDelta == 14) {
                optionDelta = 269 + ((buffer.readByte() & 0xFF) << 8) + (buffer.readByte() & 0xFF);
            }

            if (optionLength == 13) {
                optionLength += buffer.readByte() & 0xFF;
            } else if (optionLength == 14) {
                optionLength = 269 + ((buffer.readByte() & 0xFF) << 8) + (buffer.readByte() & 0xFF);
            }


            int actualOptionNumber = previousOptionNumber + optionDelta;

            try {
                byte[] optionValue = new byte[optionLength];
                buffer.readBytes(optionValue);
                options.put(actualOptionNumber, optionValue);

            } catch (IllegalArgumentException e) {

            }

            previousOptionNumber = actualOptionNumber;

            if (buffer.readableBytes() > 0) {
                firstByte = buffer.readByte() & 0xFF;
            } else {
                // this is necessary if there is no payload and the last option is empty (e.g. UintOption with value 0)
                firstByte = 0xFF;
            }

        }
        return options;
    }
}

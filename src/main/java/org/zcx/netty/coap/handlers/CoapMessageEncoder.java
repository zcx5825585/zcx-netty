package org.zcx.netty.coap.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;
import org.springframework.util.StringUtils;
import org.zcx.netty.coap.entity.CoapMessage;
import org.zcx.netty.coap.entity.CoapMessageOptions;

import java.util.List;
import java.util.stream.Collectors;

public class CoapMessageEncoder extends MessageToMessageEncoder<CoapMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, CoapMessage msg, List<Object> out) throws Exception {
        ByteBuf buf = doEncode(msg);
        out.add(new DatagramPacket(buf, msg.getSender()));
    }

    protected ByteBuf doEncode(CoapMessage msg) throws Exception {
        ByteBuf buf = Unpooled.buffer();
        encodeHeader(buf, msg);
        encodeOptions(buf, msg.getOptions());
        if (!StringUtils.isEmpty(msg.getPayload())) {
            buf.writeByte(255);//payload开始的标识
            byte[] bytes = msg.getPayload().getBytes(CharsetUtil.UTF_8);
            buf.writeBytes(bytes);
        }
        return buf;
    }

    protected void encodeOptions(ByteBuf buffer, CoapMessageOptions options) {

        //Encode options one after the other and append buf option to the buf
        int previousOptionNumber = 0;

        List<Integer> optionNumberList = options.keySet().stream().sorted().collect(Collectors.toList());

        for (Integer optionNumber : optionNumberList) {
            byte[] optionValue = options.get(optionNumber);
            encodeOption(buffer, optionNumber, optionValue, previousOptionNumber);
            previousOptionNumber = optionNumber;
        }
    }

    protected void encodeOption(ByteBuf buffer, int optionNumber, byte[] optionValue, int prevNumber) {
        int optionDelta = optionNumber - prevNumber;
        int optionLength = optionValue.length;


        if (optionDelta < 13) {
            //option delta < 13
            if (optionLength < 13) {
                buffer.writeByte(((optionDelta & 0xFF) << 4) | (optionLength & 0xFF));
            } else if (optionLength < 269) {
                buffer.writeByte(((optionDelta << 4) & 0xFF) | (13 & 0xFF));
                buffer.writeByte((optionLength - 13) & 0xFF);
            } else {
                buffer.writeByte(((optionDelta << 4) & 0xFF) | (14 & 0xFF));
                buffer.writeByte(((optionLength - 269) & 0xFF00) >>> 8);
                buffer.writeByte((optionLength - 269) & 0xFF);
            }
        } else if (optionDelta < 269) {
            //13 <= option delta < 269
            if (optionLength < 13) {
                buffer.writeByte(((13 & 0xFF) << 4) | (optionLength & 0xFF));
                buffer.writeByte((optionDelta - 13) & 0xFF);
            } else if (optionLength < 269) {
                buffer.writeByte(((13 & 0xFF) << 4) | (13 & 0xFF));
                buffer.writeByte((optionDelta - 13) & 0xFF);
                buffer.writeByte((optionLength - 13) & 0xFF);
            } else {
                buffer.writeByte((13 & 0xFF) << 4 | (14 & 0xFF));
                buffer.writeByte((optionDelta - 13) & 0xFF);
                buffer.writeByte(((optionLength - 269) & 0xFF00) >>> 8);
                buffer.writeByte((optionLength - 269) & 0xFF);
            }
        } else {
            //269 <= option delta < 65805
            if (optionLength < 13) {
                buffer.writeByte(((14 & 0xFF) << 4) | (optionLength & 0xFF));
                buffer.writeByte(((optionDelta - 269) & 0xFF00) >>> 8);
                buffer.writeByte((optionDelta - 269) & 0xFF);
            } else if (optionLength < 269) {
                buffer.writeByte(((14 & 0xFF) << 4) | (13 & 0xFF));
                buffer.writeByte(((optionDelta - 269) & 0xFF00) >>> 8);
                buffer.writeByte((optionDelta - 269) & 0xFF);
                buffer.writeByte((optionLength - 13) & 0xFF);
            } else {
                buffer.writeByte(((14 & 0xFF) << 4) | (14 & 0xFF));
                buffer.writeByte(((optionDelta - 269) & 0xFF00) >>> 8);
                buffer.writeByte((optionDelta - 269) & 0xFF);
                buffer.writeByte(((optionLength - 269) & 0xFF00) >>> 8);
                buffer.writeByte((optionLength - 269) & 0xFF);
            }
        }

        //Write option value
        buffer.writeBytes(optionValue);
    }


    protected void encodeHeader(ByteBuf buffer, CoapMessage coapMessage) {

        byte[] token = coapMessage.getToken();

        int encodedHeader = ((coapMessage.getVersion() & 0x03) << 30)
                | ((coapMessage.getMessageType() & 0x03) << 28)
                | ((coapMessage.getTokenLength() & 0x0F) << 24)
                | ((coapMessage.getMessageCode() & 0xFF) << 16)
                | ((coapMessage.getMessageID() & 0xFFFF));

        buffer.writeInt(encodedHeader);

        //Write token
        if (token.length > 0) {
            buffer.writeBytes(token);
        }
    }
}

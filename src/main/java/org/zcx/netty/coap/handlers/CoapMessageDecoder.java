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

    //  |       0       |       1       |       2       |       3       |
    //  |7 6 5 4 3 2 1 0|7 6 5 4 3 2 1 0|7 6 5 4 3 2 1 0|7 6 5 4 3 2 1 0|
    //	+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    //	|Ver| T |  TKL  |      Code     |          Message ID           |
    //	+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    //	|   Token (if any, TKL bytes) ...
    //	+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    //	|   Options (if any) ...
    //	+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    //	|1 1 1 1 1 1 1 1|    Payload (if any) ...
    //	+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    protected CoapMessage decode(ByteBuf in) throws Exception {
        CoapMessage coapMessage = new CoapMessage();
        ByteBuf buf = in;
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
        buf.readBytes(token);//根据tokenLength读取token
        String tokenStr = HexUtil.encodeHexStr(token);
        coapMessage.setToken(token);

        if (buf.readableBytes() > 0) {
            //如果未读取完，尝试读取options
            CoapMessageOptions options = readOptions(buf);
            coapMessage.setOptions(options);
        }
        buf.discardReadBytes();
        String payload = buf.toString(CharsetUtil.UTF_8);
        coapMessage.setPayload(payload);

        buf.skipBytes(buf.readableBytes());

        return coapMessage;
    }

    //    7   6   5   4   3   2   1   0
    //	+---------------+---------------+
    //	|               |               |
    //	|  Option Delta | Option Length |   1 byte
    //	|               |               |
    //	+---------------+---------------+
    //	\                               \
    //	/         Option Delta          /   0-2 bytes
    //	\          (extended)           \
    //	+-------------------------------+
    //	\                               \
    //	/         Option Length         /   0-2 bytes
    //	\          (extended)           \
    //	+-------------------------------+
    //	\                               \
    //	/                               /
    //	\                               \
    //	/         Option Value          /   0 or more bytes
    //	\                               \
    //	/                               /
    //	\                               \
    //	+-------------------------------+
    private CoapMessageOptions readOptions(ByteBuf buffer) {
        CoapMessageOptions options = new CoapMessageOptions();

        //Decode the options
        //所有的option必须按实际option编号的递增排列，某一个option和上一个option之间的option编号差值为delta
        int previousOptionNumber = 0;
        int firstByte = buffer.readByte() & 0xFF;

        //payload 前带有标识 11111111 即 0xFF
        while (firstByte != 0xFF && buffer.readableBytes() >= 0) {
            int optionDelta = (firstByte & 0xF0) >>> 4;
            int optionLength = firstByte & 0x0F;

            //*	Option Delta：4-bit无符号整型。值0-12代表option delta。其它3个值作为特殊情况保留：
            //  *	当值为**13**：有一个8-bit无符号整型（extended）跟随在第一个字节之后，本option的实际delta是这个8-bit值加13。
            //	*	当值为**14**：有一个16-bit无符号整型（网络字节序）（extended）跟随在第一个字节之后，本option的实际delta是这个16-bit值加269。
            //	*	当值为**15**：保留为将来使用。如果这个字段被设置为值15，必须当作消息格式错误来处理。//todo 未处理
            if (optionDelta == 13) {
                optionDelta += buffer.readByte() & 0xFF;
            } else if (optionDelta == 14) {
                optionDelta = 269 + ((buffer.readByte() & 0xFF) << 8) + (buffer.readByte() & 0xFF);
            }

            //*	Option Length：4-bit无符号整数。值0-12代表这个option值的长度，单位是字节。其它3个值是特殊保留的：
            //	*	当值为**13**：有一个8-bit无符号整型跟随在第一个字节之后，本option的实际长度是这个8-bit值加13。
            //	*	当值为**14**：一个16-bit无符号整型（网络字节序）跟随在第一个字节之后，本option的实际长度是这个16-bit值加269。
            //	*	当值为**15**：保留为将来使用。如果这个字段被设置为值15，必须当作消息格式错误来处理。//todo 未处理
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
                //当最后一项optionLength为0时且没有payload时，buffer.readableBytes()=0，如果不更新firstByte，会再次进入循环，此处也可直接使用break
                firstByte = 0xFF;
            }

        }
        return options;
    }
}

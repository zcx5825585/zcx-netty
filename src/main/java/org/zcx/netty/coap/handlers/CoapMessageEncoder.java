package org.zcx.netty.coap.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;
import org.zcx.netty.coap.entity.CoapMessage;
import org.zcx.netty.coap.entity.CoapMessageOptions;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

public class CoapMessageEncoder extends MessageToMessageEncoder<CoapMessage> {
    private final Log log = LogFactory.getLog(this.getClass());

    @Override
    protected void encode(ChannelHandlerContext ctx, CoapMessage msg, List<Object> out) throws Exception {
        log.debug(String.format("响应数据：%s", msg));
        ByteBuf buf = doEncode(msg);
        if (msg.getGroupSenders() == null || msg.getGroupSenders().isEmpty()) {
            out.add(new DatagramPacket(buf, msg.getSender()));
        }else {
            for (InetSocketAddress oneSender : msg.getGroupSenders()) {
                out.add(new DatagramPacket(buf, oneSender));
            }
        }
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
    protected ByteBuf doEncode(CoapMessage msg) throws Exception {
        ByteBuf buf = Unpooled.buffer();
        encodeHeader(buf, msg);
        encodeOptions(buf, msg.getOptions());
        if (!StringUtils.isEmpty(msg.getPayload())) {
            buf.writeByte(0xff);//payload开始的标识
            byte[] bytes = msg.getPayload().getBytes(CharsetUtil.UTF_8);
            buf.writeBytes(bytes);
        }
        return buf;
    }

    protected void encodeOptions(ByteBuf buffer, CoapMessageOptions options) {

        //Encode options one after the other and append buf option to the buf
        int previousOptionNumber = 0;

        //所有的option必须按实际option编号的递增排列，某一个option和上一个option之间的option编号差值为delta
        List<Integer> optionNumberList = options.keySet().stream().sorted().collect(Collectors.toList());

        for (Integer optionNumber : optionNumberList) {
            byte[] optionValue = options.get(optionNumber);
            encodeOption(buffer, optionNumber, optionValue, previousOptionNumber);
            previousOptionNumber = optionNumber;
        }
    }

    private int[] turnNum(int num) {
        int[] arr = new int[3];
        if (num < 13) {
            arr[0] = num & 0xFF;
        } else if (num < 269) {
            arr[0] = 13 & 0xFF;
            arr[1] = (num - 13) & 0xFF;
        } else {
            arr[0] = 14 & 0xFF;
            arr[1] = ((num - 269) & 0xFF00) >>> 8;
            arr[2] = (num - 269) & 0xFF;
        }
        return arr;
    }

    protected void encodeOption(ByteBuf buffer, int optionNumber, byte[] optionValue, int prevNumber) {
        int optionDelta = optionNumber - prevNumber;
        int optionLength = optionValue.length;

        int[] deltaArr = turnNum(optionDelta);
        int[] lengthArr = turnNum(optionLength);
        int first = ((deltaArr[0]) << 4) | (lengthArr[0]);
        buffer.writeByte(first);
        if (deltaArr[1] > 0) {
            buffer.writeByte(deltaArr[1]);
        }
        if (deltaArr[2] > 0) {
            buffer.writeByte(deltaArr[2]);
        }
        if (lengthArr[1] > 0) {
            buffer.writeByte(lengthArr[1]);
        }
        if (lengthArr[2] > 0) {
            buffer.writeByte(lengthArr[2]);
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

package org.zcx.netty.coap.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zcx.netty.coap.common.CoapOptionType;
import org.zcx.netty.coap.entity.CoapMessage;
import org.zcx.netty.coap.entity.CoapMessageOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerBlock2Handler extends MessageToMessageCodec<CoapMessage, CoapMessage> {
    private final Log log = LogFactory.getLog(this.getClass());
    private Map<String, StringBuffer> payloadMap = new HashMap<>();

    @Override
    protected void encode(ChannelHandlerContext ctx, CoapMessage coapMessage, List<Object> out) throws Exception {
        if (false) {
            //如果payload过大 拆解
        } else {
            out.add(coapMessage);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, CoapMessage coapMessage, List<Object> out) throws Exception {
        CoapMessageOptions options = coapMessage.getOptions();
        if (options.containsKey(CoapOptionType.BLOCK_2)) {
            //todo 发送后续消息
        } else {
            out.add(coapMessage);
        }
    }
}

package org.zcx.netty.coap.handlers;

import cn.hutool.core.util.HexUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zcx.netty.coap.entity.CoapMessage;
import org.zcx.netty.coap.entity.CoapMessageOptions;
import org.zcx.netty.coap.common.CoapOptionType;
import org.zcx.netty.coap.utils.BytesUtils;

import java.util.HashMap;
import java.util.Map;

public class Block1Handler extends SimpleChannelInboundHandler<CoapMessage> {
    private final Log log = LogFactory.getLog(this.getClass());
    private Map<String, StringBuffer> payloadMap = new HashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CoapMessage coapMessage) throws Exception {
        CoapMessageOptions options = coapMessage.getOptions();
        if (options.containsKey(CoapOptionType.BLOCK_1)) {
            byte[] block = options.get(CoapOptionType.BLOCK_1);
            int[] result = BytesUtils.handlerBlock(block);
            log.debug(String.format("NUM: %s", result[0]));
            log.debug(String.format("M: %s", result[1]));
            log.debug(String.format("SZX: %s", (1 << (result[2] + 4))));

            String tokenStr = HexUtil.encodeHexStr(coapMessage.getToken());
            if (!payloadMap.containsKey(tokenStr)) {
                payloadMap.put(tokenStr, new StringBuffer());
                if (options.containsKey(CoapOptionType.SIZE_1)) {
                    byte[] blockSize = options.get(CoapOptionType.SIZE_1);
                    log.debug(String.format("Block1 size: %s", BytesUtils.bytesToInt(blockSize)));
                }
            }
            StringBuffer stringBuffer = payloadMap.get(tokenStr);
            stringBuffer.append(coapMessage.getPayload());
            if (result[1] == 1) {
                CoapMessage ack = coapMessage.createAck(95);
                ack.setPayload("");
                log.debug(String.format("block1生成数据：%s", ack.toString()));
                ctx.writeAndFlush(ack);
            } else {
                coapMessage.setPayload(stringBuffer.toString());
                payloadMap.remove(tokenStr);
                ctx.fireChannelRead(coapMessage);
            }
        } else {
            ctx.fireChannelRead(coapMessage);
        }
    }
}

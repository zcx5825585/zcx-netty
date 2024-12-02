package org.zcx.netty.coap.handlers;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zcx.netty.coap.common.CoapMessageType;
import org.zcx.netty.coap.entity.CoapMessage;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class CoapRstHandler extends MessageToMessageCodec<CoapMessage, CoapMessage> {
    private final Log log = LogFactory.getLog(this.getClass());

    //可以设置过期时间的本地键值对集合
    private Cache<Integer, CoapMessage> messageCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            // 初始的缓存空间大小
            .initialCapacity(10)
            // 缓存的最大条数
            .maximumSize(500)
            .build();

    @Override
    protected void encode(ChannelHandlerContext ctx, CoapMessage coapMessage, List<Object> out) throws Exception {
            messageCache.put(coapMessage.getMessageID(), coapMessage);
            out.add(coapMessage);
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, CoapMessage coapMessage, List<Object> out) throws Exception {
        if (CoapMessageType.RST == coapMessage.getMessageType()) {
            CoapMessage message = messageCache.getIfPresent(coapMessage.getMessageID());
            if (message != null) {
                log.debug(String.format("重发数据：%s", message.toString()));
                ctx.writeAndFlush(message);
            }
        } else {
            out.add(coapMessage);
        }
    }

}

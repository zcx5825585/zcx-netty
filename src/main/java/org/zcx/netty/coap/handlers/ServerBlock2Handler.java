package org.zcx.netty.coap.handlers;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zcx.netty.coap.common.CoapMessageCode;
import org.zcx.netty.coap.common.CoapOptionType;
import org.zcx.netty.coap.entity.CoapBlock;
import org.zcx.netty.coap.entity.CoapMessage;
import org.zcx.netty.coap.entity.CoapMessageOptions;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ServerBlock2Handler extends MessageToMessageCodec<CoapMessage, CoapMessage> {
    private final Log log = LogFactory.getLog(this.getClass());

    //可以设置过期时间的本地键值对集合
    public Cache<String, CoapBlock> payloadMap = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            // 初始的缓存空间大小
            .initialCapacity(10)
            // 缓存的最大条数
            .maximumSize(500)
            .build();

    private static final int BLOCK2_MAX_SIZE = 512;


    @Override
    protected void decode(ChannelHandlerContext ctx, CoapMessage coapMessage, List<Object> out) throws Exception {
        CoapMessageOptions options = coapMessage.getOptions();
        if (options != null && options.containsKey(CoapOptionType.BLOCK_2)) {
            //收到后续消息请求时发送后续消息
            byte[] blockOption = options.get(CoapOptionType.BLOCK_2);
            CoapBlock requestBlock = new CoapBlock(blockOption);
            //通过token判断是否是同一条消息
            String cacheKey = coapMessage.getCacheKey();
            //消息中会携带需要相应的num
            int requestNUM = requestBlock.getNUM();
            int requestSZX = requestBlock.getSZX();
//            log.debug(String.format("接收到block2消息：token=%s;block1(szx=%s, m=%s, num=%s)", tokenStr, requestBlock.getSZX(), requestBlock.getM(), requestNUM));

            if (requestNUM == 0) {//客户端请求携带block2选项，根据block2选项设置块大小
                CoapBlock requestBlock2 = CoapBlock.requestBlock2(requestBlock.getSZX());
                payloadMap.put(cacheKey, requestBlock2);
                out.add(coapMessage);
            } else {
                CoapBlock block = payloadMap.getIfPresent(cacheKey);
                if (block != null) {
                    CoapMessage ack = coapMessage.createAck(CoapMessageCode.CONTENT_205);
                    //根据请求携带的 block2信息重设num和szx
                    block.resetOffset(requestNUM,256);//是否会多次变化？
                    ack.getOptions().putObject(CoapOptionType.BLOCK_2, block);
                    ack.setPayload(block.getCurrentPayload());//根据当前设置的num和szx截取消息
//                        log.debug(String.format("block2生成数据：%s", ack.toString()));
                    ctx.writeAndFlush(ack);
//                    }
                }
            }
        } else {
            out.add(coapMessage);
        }
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, CoapMessage coapMessage, List<Object> out) throws Exception {
        out.add(doEncode(coapMessage));
    }

    protected CoapMessage doEncode(CoapMessage coapMessage) throws Exception {
        int payloadSize = Optional.ofNullable(coapMessage.getPayload()).map(String::length).orElse(0);
        String cacheKey = coapMessage.getCacheKey();
        int oneBlockSize = BLOCK2_MAX_SIZE;
        CoapBlock requestBlock2 = payloadMap.getIfPresent(cacheKey);
        if (requestBlock2 != null) {
            if (requestBlock2.getNUM() < 0 && requestBlock2.getSZX() > 0) {//num<0表示是客户端设置的szx
                oneBlockSize = Math.min(requestBlock2.getSZX(),oneBlockSize);//使用客户端设置值和默认值中较小的那个
            }
        }
        if (payloadSize > oneBlockSize) {
            //如果payload过大 拆解，发送第一条，并等待后续消息请求
            String payload = coapMessage.getPayload();
            int NUM = 0;
            StringBuffer stringBuffer = new StringBuffer(payload);
            CoapBlock firstBlock = new CoapBlock(NUM, oneBlockSize, stringBuffer);
            payloadMap.put(cacheKey, firstBlock);//缓存消息
            coapMessage.getOptions().putObject(CoapOptionType.BLOCK_2, firstBlock);
            coapMessage.getOptions().putObject(CoapOptionType.SIZE_2, payloadSize);
            coapMessage.setPayload(firstBlock.getCurrentPayload());//根据当前设置的num和szx截取消息
            return coapMessage;
        } else {
            return coapMessage;
        }
    }

}

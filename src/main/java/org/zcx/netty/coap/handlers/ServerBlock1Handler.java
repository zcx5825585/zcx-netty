package org.zcx.netty.coap.handlers;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zcx.netty.coap.common.CoapMessageCode;
import org.zcx.netty.coap.common.CoapOptionType;
import org.zcx.netty.coap.entity.CoapBlock;
import org.zcx.netty.coap.entity.CoapMessage;
import org.zcx.netty.coap.entity.CoapMessageOptions;
import org.zcx.netty.coap.utils.BytesUtils;

import java.util.concurrent.TimeUnit;

public class ServerBlock1Handler extends SimpleChannelInboundHandler<CoapMessage> {
    private final Log log = LogFactory.getLog(this.getClass());

    //可以设置过期时间的本地键值对集合
    private Cache<String, CoapBlock> payloadMap = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            // 初始的缓存空间大小
            .initialCapacity(10)
            // 缓存的最大条数
            .maximumSize(500)
            .build();

    private static int BLOCK1_MAX_SIZE = 256;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CoapMessage coapMessage) throws Exception {
        CoapMessageOptions options = coapMessage.getOptions();
        if (options == null || !options.containsKey(CoapOptionType.BLOCK_1)) {
            //不是block1消息，跳过处理
            ctx.fireChannelRead(coapMessage);
        } else {
            byte[] blockOption = options.get(CoapOptionType.BLOCK_1);
            CoapBlock currentBlock = new CoapBlock(blockOption);
            if (options.containsKey(CoapOptionType.SIZE_1)) {
                byte[] blockSize = options.get(CoapOptionType.SIZE_1);
                currentBlock.setBlockSize(BytesUtils.bytesToInt(blockSize));
            }
            currentBlock.setPayload(new StringBuffer(coapMessage.getPayload()));
            String caCheKey = coapMessage.getCacheKey();
//            log.debug(String.format("接收到block1消息：token=%s;block1(szx=%s, m=%s, num=%s)", tokenStr, currentBlock.getSZX(), currentBlock.getM(), currentBlock.getNUM()));

            int code = mergeBlock(caCheKey, currentBlock);
            switch (code) {
                case -1:
                    break;
                case CoapMessageCode.REQUEST_ENTITY_INCOMPLETE_408:
                    CoapMessage errorAck = coapMessage.createAck(CoapMessageCode.REQUEST_ENTITY_INCOMPLETE_408);
                    errorAck.setPayload("wrong block number");
//                    log.debug(String.format("block1生成数据：%s", ack.toString()));
                    payloadMap.invalidate(caCheKey);
                    ctx.writeAndFlush(errorAck);
                    break;
                case CoapMessageCode.CONTINUE_231:
                    if (1 == currentBlock.getM()) {//后续有些其他消息
                        payloadMap.put(caCheKey, currentBlock);//更新已有的block
                        CoapMessage ack = coapMessage.createAck(CoapMessageCode.CONTINUE_231);
                        CoapBlock requestBlock = currentBlock.copy();
                        //请求的szx大于可接受大小，重设大小
                        if (requestBlock.getSZX() > BLOCK1_MAX_SIZE) {//todo 是否会多次变化？
                            requestBlock.setSZX(BLOCK1_MAX_SIZE);
                            ack.getOptions().putObject(CoapOptionType.BLOCK_1, requestBlock);
                        }
                        ack.setPayload("");
//                        log.debug(String.format("block1生成数据：%s", ack.toString()));
                        ctx.writeAndFlush(ack);
                    } else {//已经是最后一条
                        payloadMap.invalidate(caCheKey);
                        coapMessage.setPayload(currentBlock.getPayload().toString());
                        ctx.fireChannelRead(coapMessage);
                    }
                    break;
            }
        }
    }


    //不支持乱序消息
    //RFC 7959是CoAP分块传输的官方标准，它引入了Block1和Block2选项来处理分块传输。然而，该标准并没有明确说明是否支持乱序传输。从标准的角度来看，它更倾向于保证消息的有序传输，以便接收端能够正确重组消息。
    //返回95为成功， 返回136为序号num错误 返回其他为丢弃
    private int mergeBlock(String tokenStr, CoapBlock currentBlock) {
        CoapBlock oldBlock = payloadMap.getIfPresent(tokenStr);
        if (oldBlock != null) {//和已有block合并
            if (currentBlock.getNUM() - oldBlock.getResizeNUM(currentBlock.getSZX()) < 1) {
                //重复接收，丢弃
                return -1;
            } else if (currentBlock.getNUM() - oldBlock.getResizeNUM(currentBlock.getSZX()) > 1) {
                //不连续 返回报错
                return CoapMessageCode.REQUEST_ENTITY_INCOMPLETE_408;
            } else {
                currentBlock.setBlockSize(oldBlock.getBlockSize());//设置size
                currentBlock.getPayload().insert(0, oldBlock.getPayload());//拼接payload
                return CoapMessageCode.CONTINUE_231;
            }
        } else {//新的block
            if (currentBlock.getNUM() > 0) {
                //不是第一个 返回报错
                return CoapMessageCode.REQUEST_ENTITY_INCOMPLETE_408;
            } else {
                return CoapMessageCode.CONTINUE_231;
            }
        }
    }
}

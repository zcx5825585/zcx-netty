package org.zcx.netty.coap.handlers;

import cn.hutool.core.util.HexUtil;
import com.github.benmanes.caffeine.cache.Cache;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zcx.netty.coap.common.CoapOptionType;
import org.zcx.netty.coap.entity.CoapBlock;
import org.zcx.netty.coap.entity.CoapMessage;
import org.zcx.netty.coap.entity.CoapMessageOptions;
import org.zcx.netty.coap.utils.BytesUtils;

import javax.annotation.Resource;

public class ServerBlock1Handler extends SimpleChannelInboundHandler<CoapMessage> {
    private final Log log = LogFactory.getLog(this.getClass());

    @Resource(name = "blockCache")
    private Cache<String, CoapBlock> payloadMap;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CoapMessage coapMessage) throws Exception {
        CoapMessageOptions options = coapMessage.getOptions();
        if (!options.containsKey(CoapOptionType.BLOCK_1)) {
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
            String tokenStr = HexUtil.encodeHexStr(coapMessage.getToken());
            log.debug(String.format("接收到block1消息：token=%s;block1(szx=%s, m=%s, num=%s)", tokenStr, currentBlock.getSZX(), currentBlock.getM(), currentBlock.getNUM()));

            int result = margeBlock(tokenStr, currentBlock);
            if (-1 == result) {
                return;
            } else if (1 == result) {
                //其他项目未返回RST，而是返回了错误136：请求实体不完整
                CoapMessage ack = coapMessage.createAck(136);
                ack.setPayload("wrong block number");
                log.debug(String.format("block1生成数据：%s", ack.toString()));
                payloadMap.invalidate(tokenStr);
                ctx.writeAndFlush(ack);
            } else {
                if (1 == currentBlock.getM()) {//后续有些其他消息
                    payloadMap.put(tokenStr, currentBlock);//更新已有的block
                    CoapMessage ack = coapMessage.createAck(95);
                    ack.setPayload("");
                    log.debug(String.format("block1生成数据：%s", ack.toString()));
                    ctx.writeAndFlush(ack);
                } else {//已经是最后一条
                    payloadMap.invalidate(tokenStr);
                    coapMessage.setPayload(currentBlock.getPayload().toString());
                    ctx.fireChannelRead(coapMessage);
                }
            }
        }
    }

    //不支持乱序消息
    //RFC 7959是CoAP分块传输的官方标准，它引入了Block1和Block2选项来处理分块传输。然而，该标准并没有明确说明是否支持乱序传输。从标准的角度来看，它更倾向于保证消息的有序传输，以便接收端能够正确重组消息。
    private int margeBlock(String tokenStr, CoapBlock currentBlock) {
        CoapBlock oldBlock = payloadMap.getIfPresent(tokenStr);
        if (oldBlock != null) {//和已有block合并
            if (currentBlock.getNUM() - oldBlock.getNUM() < 1) {
                //重复接收，丢弃
                return -1;
            } else if (currentBlock.getNUM() - oldBlock.getNUM() > 1) {
                //不连续 返回报错
                return 1;
            }
            currentBlock.setBlockSize(oldBlock.getBlockSize());//设置size
            currentBlock.getPayload().insert(0, oldBlock.getPayload());//拼接payload
        } else {//新的block
            if (currentBlock.getNUM() > 0) {
                //不是第一个 返回报错
                return 1;
            }
        }
        return 0;
    }
}

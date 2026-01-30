package org.zcx.netty.coap.handlers;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zcx.netty.coap.common.CoapMessageCode;
import org.zcx.netty.coap.common.CoapMessageType;
import org.zcx.netty.coap.common.CoapOptionType;
import org.zcx.netty.coap.entity.CoapMessage;
import org.zcx.netty.coap.entity.CoapMessageOptions;
import org.zcx.netty.coap.utils.BytesUtils;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class ServerObserveHandler extends SimpleChannelInboundHandler<CoapMessage> {
    private final Log log = LogFactory.getLog(this.getClass());

    private ChannelHandlerContext ctx;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        super.channelActive(ctx);
    }

    //可以设置过期时间的本地键值对集合
    private Cache<String, CoapMessage> messageCache = Caffeine.newBuilder()
//            .expireAfterWrite(1, TimeUnit.MINUTES)
            // 初始的缓存空间大小
            .initialCapacity(10)
            // 缓存的最大条数
            .maximumSize(500)
            .build();

    public void upgrade(String resourceUri, String payload) {
        CoapMessage subscription = messageCache.getIfPresent(resourceUri);
        if (subscription != null) {
            CoapMessageOptions options = subscription.getOptions();
            int oldVersion = BytesUtils.bytesToInt(options.get(6));
            if (oldVersion < 2) {
                oldVersion = 2;
            } else {
                oldVersion++;
            }
            options.putObject(6, oldVersion);
            subscription.setMessageID(BytesUtils.getNextMessageId(subscription.getMessageID()));
            subscription.setMessageType(CoapMessageType.NON);
            subscription.setPayload(payload);
            messageCache.put(resourceUri, subscription);
            ctx.writeAndFlush(subscription);
        }
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CoapMessage coapMessage) throws Exception {
        CoapMessageOptions options = coapMessage.getOptions();
        if (options != null && options.containsKey(CoapOptionType.OBSERVE)) {
            byte[] observeArr = options.get(CoapOptionType.OBSERVE);
            int observe = 0;
            if (observeArr.length > 0) {
                observe = BytesUtils.bytesToInt(options.get(CoapOptionType.OBSERVE));
            }

            if (0 == observe) {//0为订阅
                CoapMessage ack = coapMessage.createAck(CoapMessageCode.CONTENT_205);
                CoapMessageOptions ackOptions = ack.getOptions();
                ackOptions.putObject(CoapOptionType.OBSERVE, 0);
                ackOptions.putEmpty(CoapOptionType.CONTENT_FORMAT);
                String senderString = ack.getSenderString();
                String resourceUri = coapMessage.getResourceUri();
                ack.setPayload(senderString + " subscription successful,resource:" + resourceUri);
//                        String cacheKey = resourceUri+"-"+senderString;
                log.debug(String.format("生成：%s", ack));
                ctx.writeAndFlush(ack);
                CoapMessage subscription = messageCache.getIfPresent(resourceUri);
                if (subscription == null) {
                    subscription = ack;
                    subscription.setGroupSenders(new ArrayList<>());
                    messageCache.put(resourceUri, subscription);
                } else {
                    CoapMessageOptions oldOption = subscription.getOptions();
                    int oldVersion = BytesUtils.bytesToInt(oldOption.get(CoapOptionType.OBSERVE));
                    //已经有数据了，返回一次
                    ack.setPayload(subscription.getPayload());
                    ack.setMessageID(BytesUtils.getNextMessageId(ack.getMessageID()));
                    ack.setMessageType(CoapMessageType.NON);
                    ack.getOptions().putObject(CoapOptionType.OBSERVE, oldVersion);
                    Thread.sleep(500);//消息已发送，间隔太短客户端会丢弃消息，需要添加一段间隔   为什么丢弃？
                    log.debug(String.format("生成：%s", ack));
                    ctx.writeAndFlush(ack);
                }
                subscription.getGroupSenders().add(ack.getSender());
            } else if (1 == observe) {
                String resourceUri = coapMessage.getResourceUri();
                CoapMessage subscription = messageCache.getIfPresent(resourceUri);
                List<InetSocketAddress> groupSenders = subscription.getGroupSenders();
                int index = -1;
                for (int i = 0; i < groupSenders.size(); i++) {
                    InetSocketAddress one = groupSenders.get(i);
                    if (coapMessage.getSender().equals(one)) {
                        index = i;
                        break;
                    }
                }
                if (index >= 0) {
                    groupSenders.remove(index);
                }
            }

        } else {
            ctx.fireChannelRead(coapMessage);
        }
    }
}

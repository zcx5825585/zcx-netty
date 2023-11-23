package org.zcx.netty.depended.mqtt;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: zhouwenjie
 * @description: 对接收到的消息进行业务处理
 * @create: 2023-04-07 16:29
 * CONNECT	    1	    C->S	客户端请求与服务端建立连接 （服务端接收）
 * CONNACK	    2	    S->C	服务端确认连接建立（客户端接收）
 * PUBLISH	    3	    CóS	    发布消息 （服务端接收【QoS 0级别，最多分发一次】）-->生产者只会发送一次消息，不关心消息是否被代理服务端或消费者收到
 * PUBACK	    4	    CóS	    收到发布消息确认（客户端接收【QoS 1级别，至少分发一次】） -->保证消息发送到服务端（也就是代理服务器broker），如果没收到或一定时间没收到服务端的ack，就会重发消息
 * PUBREC	    5	    CóS	    收到发布消息（客户端接收【QoS 2级别】）|
 * PUBREL	    6	    CóS	    释放发布消息（服务端接收【QoS 2级别】）|只分发一次消息，且保证到达 -->这三步保证消息有且仅有一次传递给消费者
 * PUBCOMP	    7	    CóS	    完成发布消息（客户端接收【QoS 2级别】）|
 * SUBSCRIBE	8	    C->S	订阅请求（服务端接收）
 * SUBACK	    9	    S->C	订阅确认（客户端接收）
 * UNSUBSCRIBE	10	    C->S	取消订阅（服务端接收）
 * UNSUBACK	    11	    S->C	取消订阅确认（客户端接收）
 * PINGREQ	    12	    C->S	客户端发送PING(连接保活)命令（服务端接收）
 * PINGRESP	    13	    S->C	PING命令回复（客户端接收）
 * DISCONNECT	14	    C->S	断开连接 （服务端接收）
 **/

@Component
public class MqttMsgBack {
    private static final Logger log = LoggerFactory.getLogger(MqttMsgBack.class);

    private Long commonWaitTime = 30 * 1000L;


    // 记录消息id的变量，id值范围1~65535
    private final AtomicInteger nextMessageId = new AtomicInteger(1);

    // ----------------------发送消息端（客户端）可能使用的方法----------------------------------------------------------------

    /**
     * 确认连接请求
     *
     * @param ctx
     * @param mqttMessage
     */
    public void receiveConnectionAck(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttConnAckMessage mqttConnAckMessage = (MqttConnAckMessage) mqttMessage;
        MqttConnAckVariableHeader variableHeader = mqttConnAckMessage.variableHeader();
        MqttConnectReturnCode mqttConnectReturnCode = variableHeader.connectReturnCode();
        if (mqttConnectReturnCode.name().equals(MqttConnectReturnCode.CONNECTION_ACCEPTED.name())) {
            //连接成功
            log.info("服务端连接验证成功");
        } else {
            log.error("服务端连接验证失败：" + mqttConnectReturnCode.name());
        }
    }

    /**
     * 确认订阅回复
     *
     * @param ctx
     * @param mqttMessage
     */
    public void receiveSubAck(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        //删除消息重发机制
        MqttMessageIdVariableHeader variableHeader = (MqttMessageIdVariableHeader) mqttMessage.variableHeader();
        int messageId = variableHeader.messageId();
        ScheduledFuture<?> scheduledFuture = TimerData.scheduledFutureMap.remove(messageId);
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
    }

    /**
     * 确认取消订阅回复
     *
     * @param ctx
     * @param mqttMessage
     */
    public void receiveUnSubAck(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        //删除消息重发机制
        MqttMessageIdVariableHeader variableHeader = (MqttMessageIdVariableHeader) mqttMessage.variableHeader();
        int messageId = variableHeader.messageId();
        ScheduledFuture<?> scheduledFuture = TimerData.scheduledFutureMap.remove(messageId);
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
    }

    /**
     * 根据qos发布确认
     *
     * @param ctx
     * @param mqttMessage
     */
    public void receivePubAck(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttMessageIdVariableHeader variableHeader = (MqttMessageIdVariableHeader) mqttMessage.variableHeader();
        int messageId = variableHeader.messageId();
        //等级为1的情况，直接删除原始消息，取消消息重发机制
        ScheduledFuture<?> scheduledFuture = TimerData.scheduledFutureMap.remove(messageId);
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
    }

    /**
     * 根据qos发布确认
     *
     * @param ctx
     * @param mqttMessage
     */
    public void receivePubRfc(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        //等级为2的情况，收到PUBREC报文消息，先停止消息重发机制，再响应一个PUBREL报文并且构建消息重发机制
        MqttPubReplyMessageVariableHeader variableHeader = (MqttPubReplyMessageVariableHeader) mqttMessage.variableHeader();
        int messageId = variableHeader.messageId();
        //构建返回报文，固定报头
        MqttFixedHeader mqttFixedHeaderBack = new MqttFixedHeader(MqttMessageType.PUBREL, false, MqttQoS.AT_LEAST_ONCE, false, 0);
        //构建返回报文，可变报头
        MqttPubReplyMessageVariableHeader mqttPubReplyMessageVariableHeader = new MqttPubReplyMessageVariableHeader(messageId, MqttPubReplyMessageVariableHeader.REASON_CODE_OK, MqttProperties.NO_PROPERTIES);
        MqttMessage mqttMessageBack = new MqttMessage(mqttFixedHeaderBack, mqttPubReplyMessageVariableHeader);
        ctx.writeAndFlush(mqttMessageBack);
        //删除初始消息重发机制
        ScheduledFuture<?> scheduledFuture = TimerData.scheduledFutureMap.remove(messageId);
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
        ctx.writeAndFlush(mqttMessageBack).addListener(future -> {
            //构建消息重发
            cachePubrelMsg(messageId, ctx);
        });
    }

    private void cachePubrelMsg(int messageId, ChannelHandlerContext context) {
        //缓存一份消息，规定时间内没有收到ack，用作重发，重发时将isDup设置为true,代表重复消息
        //构建返回报文，固定报头
        MqttFixedHeader mqttFixedHeaderBack = new MqttFixedHeader(MqttMessageType.PUBREL, false, MqttQoS.AT_LEAST_ONCE, false, 0);
        //构建返回报文，可变报头
        MqttMessageIdVariableHeader mqttMessageIdVariableHeaderBack = MqttMessageIdVariableHeader.from(messageId);
        MqttMessage mqttMessageBack = new MqttMessage(mqttFixedHeaderBack, mqttMessageIdVariableHeaderBack);
        ScheduledFuture<?> scheduledFuture = TimerData.scheduledThreadPoolExecutor.scheduleAtFixedRate(new MonitorMsgTime(messageId, mqttMessageBack, context), commonWaitTime, commonWaitTime, TimeUnit.MILLISECONDS);
        TimerData.scheduledFutureMap.put(messageId, scheduledFuture);
    }

    /**
     * 功能描述: 接收到最后一次确认，取消上次PUBREL的消息重发机制
     *
     * @param ctx
     * @param mqttMessage
     * @return void
     * @author zhouwenjie
     * @date 2023/6/9 16:00
     */
    public void receivePubcomp(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttPubReplyMessageVariableHeader variableHeader = (MqttPubReplyMessageVariableHeader) mqttMessage.variableHeader();
        int messageId = variableHeader.messageId();
        ScheduledFuture<?> scheduledFuture = TimerData.scheduledFutureMap.remove(messageId);
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
    }

    /**
     * 心跳发送
     *
     * @param ctx
     * @param mqttMessage
     */
    public void pingReq(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        if (ctx != null && ctx.channel().isActive()) {
            MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PINGREQ, false, MqttQoS.AT_MOST_ONCE, false, 0);
            MqttMessage mqttMessageBack = new MqttMessage(fixedHeader);
            ctx.writeAndFlush(mqttMessageBack);
        } else {
            log.error("心跳提醒：服务端连接异常~");
        }
    }


    public void connect(ChannelHandlerContext ctx, String userName, String password) {
        MqttConnectVariableHeader mqttConnectVariableHeader = new MqttConnectVariableHeader("MQTT", 4, true, true, false, 0, false, true, 60);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        MqttConnectPayload connectPayload = new MqttConnectPayload(uuid, null, null, userName, password.getBytes(CharsetUtil.UTF_8));
        MqttFixedHeader mqttFixedHeaderInfo = new MqttFixedHeader(MqttMessageType.CONNECT, false, MqttQoS.AT_LEAST_ONCE, false, 0);
        MqttConnectMessage connectMessage = new MqttConnectMessage(mqttFixedHeaderInfo, mqttConnectVariableHeader, connectPayload);
        ctx.writeAndFlush(connectMessage);
    }

    /**
     * 主动发送消息
     *
     * @param topic   :主题名称
     * @param payload ：消息体
     * @param qos     ： 服务质量等级
     * @param retain  ：
     *                true:表示发送的消息需要一直持久保存（不受服务器重启影响），不但要发送给当前的订阅者，并且以后新来的订阅了此Topic name的订阅者会马上得到推送。
     *                false:仅仅为当前订阅者推送此消息。
     */
    public void publish(ChannelHandlerContext context, String topic, ByteBuf payload, MqttQoS qos, boolean retain, Long waitTime) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, false, qos, retain, 0);
        MqttPublishVariableHeader variableHeader = new MqttPublishVariableHeader(topic, getNewMessageId().messageId());
        MqttPublishMessage mqttPublishMessage = new MqttPublishMessage(fixedHeader, variableHeader, payload);
        //将消息发送给订阅的客户端
        if (context != null && context.channel().isActive()) {
            //因为ByteBuf每次发送之后就会被清空了，下次发送就拿不到payload，所以提前复制一份，客户端这里不用，因为在调用此方法的时候已经调用了Unpooled.wrappedBuffer了
            payload.retainedDuplicate();
            context.writeAndFlush(mqttPublishMessage);
            if (qos == MqttQoS.AT_LEAST_ONCE || qos == MqttQoS.EXACTLY_ONCE) {
                cachePublishMsg(qos, payload, variableHeader, fixedHeader, context, Optional.ofNullable(waitTime).orElse(commonWaitTime));
            }
        } else {
            log.error("发送消息提醒：服务端连接异常~");
        }
    }
    private void cachePublishMsg(MqttQoS qos, ByteBuf byteBuf, MqttPublishVariableHeader variableHeader, MqttFixedHeader mqttFixedHeaderInfo, ChannelHandlerContext context, Long waitTime) {
        //缓存一份消息，规定时间内没有收到ack，用作重发，重发时将isDup设置为true,代表重复消息
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, true, qos, false, mqttFixedHeaderInfo.remainingLength());
        MqttPublishMessage cachePubMessage = new MqttPublishMessage(fixedHeader, variableHeader, byteBuf);
        ScheduledFuture<?> scheduledFuture = TimerData.scheduledThreadPoolExecutor.scheduleAtFixedRate(new MonitorMsgTime(variableHeader.packetId(), cachePubMessage, context), waitTime, waitTime, TimeUnit.MILLISECONDS);
        TimerData.scheduledFutureMap.put(variableHeader.packetId(), scheduledFuture);
    }

//    public void publish(ChannelHandlerContext context, MqttPublishMessage mqttPublishMessage) {
//        //将消息发送给订阅的客户端
//        if (context != null && context.channel().isActive()) {
//            //因为ByteBuf每次发送之后就会被清空了，下次发送就拿不到payload，所以提前复制一份，客户端这里不用，因为在调用此方法的时候已经调用了Unpooled.wrappedBuffer了
//            ByteBuf payload = mqttPublishMessage.payload();
//            payload.retainedDuplicate();
//            context.writeAndFlush(mqttPublishMessage);
//            MqttQoS qos = mqttPublishMessage.fixedHeader().qosLevel();
//            if (qos == MqttQoS.AT_LEAST_ONCE || qos == MqttQoS.EXACTLY_ONCE) {
//                MqttPublishMessage cachePubMessage = mqttPublishMessage.copy();
//                cachePublishMsg(qos, cachePubMessage,context);
//            }
//        } else {
//            log.error("发送消息提醒：服务端连接异常~");
//        }
//    }
//
//    private void cachePublishMsg(MqttQoS qos, MqttPublishMessage cachePubMessage,ChannelHandlerContext context) {
//        //缓存一份消息，规定时间内没有收到ack，用作重发，重发时将isDup设置为true,代表重复消息
//        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, true, qos, false, cachePubMessage.fixedHeader().remainingLength());
//
//        MqttPublishVariableHeader  variableHeader= cachePubMessage.variableHeader();
//        ByteBuf byteBuf=cachePubMessage.payload();
//        cachePubMessage = new MqttPublishMessage(fixedHeader, variableHeader, byteBuf);
//        ScheduledFuture<?> scheduledFuture = TimerData.scheduledThreadPoolExecutor.scheduleAtFixedRate(new MonitorMsgTime(variableHeader.packetId(), cachePubMessage, context), waitTime, waitTime, TimeUnit.MILLISECONDS);
//        TimerData.scheduledFutureMap.put(variableHeader.packetId(), scheduledFuture);
//    }
    /**
     * 订阅主题
     *
     * @param topicName :主题名称
     * @param qos       ：服务端可以向此客户端发送的应用消息的最大QoS等级
     */
    public void subscribe(ChannelHandlerContext context, String topicName, MqttQoS qos) {
        //构造固定头
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.SUBSCRIBE, false, MqttQoS.AT_LEAST_ONCE, false, 0);
        MqttMessageIdVariableHeader variableHeader = getNewMessageId();
        //构造消息体,这里构建采用简单的模式（MqttTopicSubscription(String topicFilter, MqttQoS qualityOfService)）
        // 如果想用更复杂的，使用（MqttTopicSubscription(String topicFilter, MqttSubscriptionOption option)）
        MqttTopicSubscription subscription = new MqttTopicSubscription(topicName, qos);
        MqttSubscribePayload payload = new MqttSubscribePayload(Collections.singletonList(subscription));
        MqttSubscribeMessage mqttSubscribeMessage = new MqttSubscribeMessage(fixedHeader, variableHeader, payload);
        if (context != null && context.channel().isActive()) {
            //发送消息，异步发送
            context.writeAndFlush(mqttSubscribeMessage);
            //缓存消息
            MqttFixedHeader fixedHeader2 = new MqttFixedHeader(MqttMessageType.SUBSCRIBE, true, MqttQoS.AT_LEAST_ONCE, false, 0);
            MqttSubscribeMessage mqttSubscribeMessage2 = new MqttSubscribeMessage(fixedHeader2, variableHeader, payload);
            ScheduledFuture<?> scheduledFuture = TimerData.scheduledThreadPoolExecutor.scheduleAtFixedRate(new MonitorMsgTime(variableHeader.messageId(), mqttSubscribeMessage2, context), commonWaitTime, commonWaitTime, TimeUnit.MILLISECONDS);
            TimerData.scheduledFutureMap.put(variableHeader.messageId(), scheduledFuture);
        } else {
            log.error("订阅提醒：服务端连接异常~");
        }
    }

    /**
     * 取消订阅主题
     *
     * @param topicName
     */
    public void unsubscribe(ChannelHandlerContext context, String topicName) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.UNSUBSCRIBE, false, MqttQoS.AT_LEAST_ONCE, false, 0);
        MqttMessageIdVariableHeader variableHeader = getNewMessageId();
        MqttUnsubscribePayload payload = new MqttUnsubscribePayload(Collections.singletonList(topicName));
        MqttUnsubscribeMessage mqttUnsubscribeMessage = new MqttUnsubscribeMessage(fixedHeader, variableHeader, payload);
        if (context != null && context.channel().isActive()) {
            //发送消息
            context.writeAndFlush(mqttUnsubscribeMessage);
            MqttFixedHeader fixedHeader2 = new MqttFixedHeader(MqttMessageType.UNSUBSCRIBE, true, MqttQoS.AT_LEAST_ONCE, false, 0);
            MqttUnsubscribeMessage mqttUnsubscribeMessage2 = new MqttUnsubscribeMessage(fixedHeader2, variableHeader, payload);
            //缓存消息
            ScheduledFuture<?> scheduledFuture = TimerData.scheduledThreadPoolExecutor.scheduleAtFixedRate(new MonitorMsgTime(variableHeader.messageId(), mqttUnsubscribeMessage2, context), commonWaitTime, commonWaitTime, TimeUnit.MILLISECONDS);
            TimerData.scheduledFutureMap.put(variableHeader.messageId(), scheduledFuture);
        } else {
            log.error("取消订阅提醒：服务端连接异常~");
        }
    }

    /**
     * 功能描述: 获取消息id，int数，从1开始不能大于65535
     *
     * @param
     * @return io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader
     * @author zhouwenjie
     * @date 2023/6/12 16:16
     */
    private MqttMessageIdVariableHeader getNewMessageId() {
        int messageId;
        synchronized (this.nextMessageId) {
            this.nextMessageId.compareAndSet(0xffff, 1);
            messageId = this.nextMessageId.getAndIncrement();
        }
        return MqttMessageIdVariableHeader.from(messageId);
    }

    // ----------------------接收消息端（客户端）可能使用的方法----------------------------------------------------------------

    /**
     * 收到publish消息后的确认回复
     * 根据qos发布确认
     * isRetain:发布保留标识，表示服务器要保留这次推送的信息，如果有新的订阅者出现，就把这消息推送给它，如果设有那么推送至当前订阅者后释放
     *
     * @param ctx
     * @param mqttMessage
     */
    public void publishAck(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttPublishMessage mqttPublishMessage = (MqttPublishMessage) mqttMessage;
        MqttFixedHeader mqttFixedHeaderInfo = mqttPublishMessage.fixedHeader();
        MqttQoS qos = mqttFixedHeaderInfo.qosLevel();
        //返回消息给发送端
        switch (qos) {
            //至多一次
            case AT_MOST_ONCE:
                break;
            //至少一次
            case AT_LEAST_ONCE:
                //构建返回报文， 可变报头
                MqttMessageIdVariableHeader mqttMessageIdVariableHeaderBack = MqttMessageIdVariableHeader.from(mqttPublishMessage.variableHeader().packetId());
                //构建返回报文， 固定报头
                MqttFixedHeader mqttFixedHeaderBack = new MqttFixedHeader(MqttMessageType.PUBACK, mqttFixedHeaderInfo.isDup(), MqttQoS.AT_MOST_ONCE, mqttFixedHeaderInfo.isRetain(), 0x02);
                //构建PUBACK消息体
                MqttPubAckMessage pubAck = new MqttPubAckMessage(mqttFixedHeaderBack, mqttMessageIdVariableHeaderBack);
                ctx.writeAndFlush(pubAck);
                break;
            //刚好一次
            case EXACTLY_ONCE:
                //构建返回报文，固定报头
                MqttFixedHeader mqttFixedHeaderBack2 = new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.AT_MOST_ONCE, false, 0x02);
                //构建返回报文，可变报头
                MqttPubReplyMessageVariableHeader mqttPubReplyMessageVariableHeader = new MqttPubReplyMessageVariableHeader(mqttPublishMessage.variableHeader().packetId(), MqttPubReplyMessageVariableHeader.REASON_CODE_OK, MqttProperties.NO_PROPERTIES);
                MqttMessage mqttMessageBack = new MqttMessage(mqttFixedHeaderBack2, mqttPubReplyMessageVariableHeader);
                ctx.writeAndFlush(mqttMessageBack);
                break;
            default:
                break;
        }
    }

    /**
     * 发布完成 qos2
     *
     * @param ctx
     * @param mqttMessage
     */
    public void publishComp(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttMessageIdVariableHeader messageIdVariableHeader = (MqttMessageIdVariableHeader) mqttMessage.variableHeader();
        //构建返回报文， 固定报头
        MqttFixedHeader mqttFixedHeaderBack = new MqttFixedHeader(MqttMessageType.PUBCOMP, false, MqttQoS.AT_MOST_ONCE, false, 0x02);
        //构建返回报文， 可变报头
        MqttMessageIdVariableHeader mqttMessageIdVariableHeaderBack = MqttMessageIdVariableHeader.from(messageIdVariableHeader.messageId());
        MqttMessage mqttMessageBack = new MqttMessage(mqttFixedHeaderBack, mqttMessageIdVariableHeaderBack);
        ctx.writeAndFlush(mqttMessageBack);
    }
}
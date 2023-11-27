package org.zcx.netty.mqtt;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zcx.netty.mqtt.dto.MyMqttMessage;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractMqttHandler extends SimpleChannelInboundHandler<MqttMessage> {
    private final Logger log = LoggerFactory.getLogger(AbstractMqttHandler.class);

    protected Map<String, ChannelHandlerContext> channelMap = new HashMap<>();
    protected Set<String> topicMap = new HashSet<>();

    @Resource
    protected MqttMsgBack mqttMsgBack;

    public abstract String getHost();

    public abstract Integer getPort();

    public abstract String getUserName();

    public abstract String getPassword();

    public abstract String getDefaultTopic();

    public abstract void channelRead1(ChannelHandlerContext ctx, MqttMessage mqttMessage);

    public void sendMsg(String channelId, String topicName, String msg) {
        TopicUtil.validateTopicName(topicName);
        ByteBuf byteBuf = Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8);
        MyMqttMessage mqttMessage = new MyMqttMessage();
        mqttMessage.setTopic(topicName);
        mqttMessage.setPayload(byteBuf);
        sendMsg(channelId, mqttMessage);
    }

    public void sendMsg(String channelId, Object msg) {
        MyMqttMessage mqttMsg = (MyMqttMessage) msg;
        TopicUtil.validateTopicName(mqttMsg.getTopic());
        mqttMsgBack.publish(getChannel(channelId), mqttMsg.getTopic(), mqttMsg.getPayload(), mqttMsg.getQos(), mqttMsg.isRetain(), mqttMsg.getWaitTime());
    }

    protected ChannelHandlerContext getChannel(String channelId) {
        return channelMap.get(channelId);
    }

    /**
     * 连接成功
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        String channelId = ctx.channel().id().asShortText();
        channelMap.put(channelId, ctx);
        mqttMsgBack.connect(ctx, getUserName(), getPassword());
    }


    public void subscribe(String channelId, String topic) {
        if (topic != null && topic.length() > 0 && topicMap.add(topic)) {
            mqttMsgBack.subscribe(getChannel(channelId), topic, MqttQoS.AT_MOST_ONCE);
        }
    }

    public void unsubscribe(String channelId, String topic) {
        if (topicMap.remove(topic)) {
            mqttMsgBack.unsubscribe(getChannel(channelId), topic);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        log.error("[* Netty connection exception]:{}", cause.toString());
        cause.printStackTrace();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        if (null != mqttMessage) {
            log.info("接收mqtt消息：" + mqttMessage);
            MqttFixedHeader mqttFixedHeader = mqttMessage.fixedHeader();
            switch (mqttFixedHeader.messageType()) {
                // ----------------------发送消息端（客户端）可能会触发的事件----------------------------------------------------------------
                case CONNACK:
                    mqttMsgBack.receiveConnectionAck(ctx, mqttMessage);
                    //连接成功后开始订阅
                    subscribe(ctx.channel().id().asShortText(), getDefaultTopic());
                    break;
                case PUBREC:
                    mqttMsgBack.receivePubRfc(ctx, mqttMessage);
                case PUBACK:
                    //接收服务端的ack消息
                    mqttMsgBack.receivePubAck(ctx, mqttMessage);
                    break;
                case PUBCOMP:
                    mqttMsgBack.receivePubcomp(ctx, mqttMessage);
                    break;
                case SUBACK:
                    mqttMsgBack.receiveSubAck(ctx, mqttMessage);
                    break;
                case UNSUBACK:
                    mqttMsgBack.receiveUnSubAck(ctx, mqttMessage);
                    break;
                case PINGRESP:
                    //客户端发起心跳
                    mqttMsgBack.pingReq(ctx, mqttMessage);
                    break;
                // ----------------------接收消息端（客户端）可能会触发的事件----------------------------------------------------------------
                case PUBLISH:
                    channelRead1(ctx, mqttMessage);
                    //	收到消息，返回确认，PUBACK报文是对QoS 1等级的PUBLISH报文的响应,PUBREC报文是对PUBLISH报文的响应
                    mqttMsgBack.publishAck(ctx, mqttMessage);
                    break;
                case PUBREL:
                    //	释放消息，PUBREL报文是对QoS 2等级的PUBREC报文的响应,此时我们应该回应一个PUBCOMP报文
                    mqttMsgBack.publishComp(ctx, mqttMessage);
                    break;
                default:
                    break;
            }
        }
    }
}
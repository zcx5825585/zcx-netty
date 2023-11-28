package org.zcx.netty.mqtt.topicHandler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zcx.netty.common.exception.HandlerException;
import org.zcx.netty.common.utils.SpringUtils;
import org.zcx.netty.mqtt.AbstractMqttHandler;
import org.zcx.netty.mqtt.TopicUtil;

import java.util.HashMap;
import java.util.Map;

//@Component
@ChannelHandler.Sharable
public class MultiTopicMqttHandler extends AbstractMqttHandler {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected Map<String, MqttTopicHandler> topicHandlerMap = new HashMap<>();

    //value注入
    protected String host;
    protected Integer port;
    protected String userName;
    protected String password;

    public void sendMsg1(String topicName, String msg) {
        sendMsg(getChannelId(), topicName, msg);
    }

    public void subscribe0(String topicName, String topicHandlerName) {
        MqttTopicHandler topicHandler = SpringUtils.getBean(topicHandlerName, MqttTopicHandler.class);
        if (topicHandler == null){
            throw new HandlerException("mqtt消息处理类不存在");
        }
        topicHandlerMap.put(topicName, topicHandler);
        super.subscribe(getChannelId(), topicName);
    }

    @Override
    public void channelRead1(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttPublishMessage mqttPublishMessage = (MqttPublishMessage) mqttMessage;
        ByteBuf payload = mqttPublishMessage.payload();
        String message = payload.toString(CharsetUtil.UTF_8);

        String channelId = ctx.channel().id().asShortText();
        log.info("[" + channelId + "]" + "接收到mqtt消息 " + "\n" + mqttMessage.fixedHeader().toString() + "\n" + mqttMessage.variableHeader().toString() + "  \n" + message);

        String fullTopic = mqttPublishMessage.variableHeader().topicName();
        for (String topic : topicHandlerMap.keySet()) {
            if (TopicUtil.match(topic, fullTopic)) {
                MqttTopicHandler topicHandler = topicHandlerMap.get(topic);
                if (topicHandler != null) {
                    topicHandler.handleMassage(fullTopic, message);
                }
            }
        }
    }

    protected String getChannelId() {
        return channelMap.keySet().stream().findFirst().get();
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getDefaultTopic(){
        return null;
    }
}

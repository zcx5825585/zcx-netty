package org.zcx.netty.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zcx.netty.client.mqtt.dto.MyMqttMessage;
import org.zcx.netty.common.abstractHandler.AbstractMqttClientHandler;

@Component
@ChannelHandler.Sharable
public class MqttClientHandler extends AbstractMqttClientHandler {
    private static final Logger log = LoggerFactory.getLogger(MqttClientHandler.class);

    @Override
    public String[] getDefaultTopic() {
        return new String[]{"testtopic/#"};
    }

    @Override
    public String getUserName() {
        return "smartsite";
    }

    @Override
    public String getPassword() {
        return "smartsite12347988";
    }

    @Override
    public void sendMsg0(String channelId, String topicName, String msg) {
        ByteBuf byteBuf = Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8);
        MyMqttMessage mqttMessage = new MyMqttMessage();
        mqttMessage.setTopic(topicName);
        mqttMessage.setPayload(byteBuf);
        super.sendMsg(channelId, mqttMessage);
    }

    @Override
    public void channelRead1(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttPublishMessage mqttPublishMessage = (MqttPublishMessage) mqttMessage;
        MqttPublishVariableHeader variableHeader = mqttPublishMessage.variableHeader();
        ByteBuf payload = mqttPublishMessage.payload();
        String message = payload.toString(CharsetUtil.UTF_8);
        log.info(getHandlerName() +"接收到mqtt消息 " +  "\nHeader：" + variableHeader.toString() + "  payload：\n" + message);
    }

}

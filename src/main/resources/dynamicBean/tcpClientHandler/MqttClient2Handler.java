package dynamicBean.tcpClientHandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zcx.netty.depended.mqtt.dto.MyMqttMessage;
import org.zcx.netty.handler.abstractHandler.AbstractMqttClientHandler;

@Component
@ChannelHandler.Sharable
public class MqttClient2Handler extends AbstractMqttClientHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    //47.105.217.47
    //1883

    @Override
    public String getDefaultTopic() {
        return "testtopic2/#";
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
        ByteBuf payload = mqttPublishMessage.payload();
        String message = payload.toString(CharsetUtil.UTF_8);
        String topicName = mqttPublishMessage.variableHeader().topicName();

        String channelId = ctx.channel().id().asShortText();
        log.info(getHandlerName() + "[" + channelId + "]" + "接收到mqtt消息 " + "\n" + mqttMessage.fixedHeader().toString() + "\n" + mqttMessage.variableHeader().toString() + "  \n" + message);
    }
}

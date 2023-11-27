package org.zcx.netty.handler.configurableHandler;

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
import org.zcx.netty.handler.DynamicHandler;
import org.zcx.netty.mqtt.dto.MyMqttMessage;
import org.zcx.netty.handler.abstractHandler.AbstractMqttClientHandler;
import org.zcx.netty.bean.BeanParam;
import org.zcx.netty.bean.ConfigurableBean;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@ChannelHandler.Sharable
public class MqttClientHandler extends AbstractMqttClientHandler implements ConfigurableBean, DynamicHandler {
    private final Logger log = LoggerFactory.getLogger(MqttClientHandler.class);

    private String host;
    private int port;
    private String defaultTopic;
    private String userName;
    private String password;

    @Override
    public void config(Map<String, Object> param) {
        this.defaultTopic = (String) param.get("defaultTopic");
        this.userName = (String) param.get("userName");
        this.password = (String) param.get("password");
        this.host = (String) param.get("host");
        this.port = (Integer) param.get("port");
    }

    @Override
    public List<BeanParam> getParamList() {
        return Arrays.asList(
                new BeanParam("host","客户端IP", String.class),
                new BeanParam("port","端口", Integer.class),
                new BeanParam("defaultTopic","订阅主题", String.class),
                new BeanParam("userName","账号", String.class),
                new BeanParam("password","密码", String.class)
        );
    }

    @Override
    public void setBeanName(String beanName) {
        this.handlerName = beanName;
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
    public String getDefaultTopic() {
        return defaultTopic;
    }

    @Override
    public void sendMsg0(String channelId, String topicName, String msg) {
        ByteBuf byteBuf = Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8);
        MyMqttMessage mqttMessage = new MyMqttMessage();
        mqttMessage.setTopic(topicName);
        mqttMessage.setPayload(byteBuf);
        super.sendMsg(channelId, mqttMessage);
    }

    private int count = 0;

    @Override
    public void channelRead1(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        count++;
        log.info("count " + count);
        MqttPublishMessage mqttPublishMessage = (MqttPublishMessage) mqttMessage;
        ByteBuf payload = mqttPublishMessage.payload();
        String message = payload.toString(CharsetUtil.UTF_8);
        String topicName = mqttPublishMessage.variableHeader().topicName();

        String channelId = ctx.channel().id().asShortText();
        log.info(getHandlerName() + "[" + channelId + "]" + "接收到mqtt消息 " + "\n" + mqttMessage.fixedHeader().toString() + "\n" + mqttMessage.variableHeader().toString() + "  \n" + message);
    }

}

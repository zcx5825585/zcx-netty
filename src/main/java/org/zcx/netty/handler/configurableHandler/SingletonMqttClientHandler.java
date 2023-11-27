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
import org.zcx.netty.common.DynamicHandler;
import org.zcx.netty.common.bean.BeanParam;
import org.zcx.netty.common.bean.ConfigurableBean;
import org.zcx.netty.common.exception.HandlerException;
import org.zcx.netty.common.utils.SpringUtils;
import org.zcx.netty.depended.mqtt.MqttTopicHandler;
import org.zcx.netty.depended.mqtt.TopicUtil;
import org.zcx.netty.depended.mqtt.dto.MyMqttMessage;
import org.zcx.netty.handler.abstractHandler.AbstractMqttClientHandler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ChannelHandler.Sharable
public class SingletonMqttClientHandler extends AbstractMqttClientHandler implements DynamicHandler, ConfigurableBean {
    private final Logger log = LoggerFactory.getLogger(SingletonMqttClientHandler.class);

    private Map<String, MqttTopicHandler> topicHandlerMap = new HashMap<>();

    private String host = "47.105.217.47";
    private Integer port = 1883;
    private String userName = "smartsite";
    private String password = "smartsite12347988";

    @Override
    public void setBeanName(String beanName) {
        this.handlerName = beanName;
    }

    @Override
    public void config(Map<String, Object> param) {
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
                new BeanParam("userName","账号", String.class),
                new BeanParam("password","密码", String.class)
        );
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
    public String getDefaultTopic(){
        return null;
    }
    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void sendMsg1(String topicName, String msg) {
        TopicUtil.validateTopicName(topicName);
        sendMsg0(getChannelId(), topicName, msg);
    }

    public void subscribe0(String topicName, String topicHandlerName) {
        TopicUtil.validateTopicName(topicName);
        MqttTopicHandler topicHandler = SpringUtils.getBean(topicHandlerName, MqttTopicHandler.class);
        if (topicHandler == null){
            throw new HandlerException("mqtt消息处理类不存在");
        }
        topicHandlerMap.put(topicName, topicHandler);
        super.subscribe(getChannelId(), topicName);
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

        String channelId = ctx.channel().id().asShortText();
        log.info(getHandlerName() + "[" + channelId + "]" + "接收到mqtt消息 " + "\n" + mqttMessage.fixedHeader().toString() + "\n" + mqttMessage.variableHeader().toString() + "  \n" + message);

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

    private String getChannelId() {
        return channelMap.keySet().stream().findFirst().get();
    }


}

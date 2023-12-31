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
import org.zcx.netty.bean.BeanParam;
import org.zcx.netty.bean.ConfigurableBean;
import org.zcx.netty.common.exception.HandlerException;
import org.zcx.netty.common.utils.SpringUtils;
import org.zcx.netty.mqtt.topicHandler.MqttTopicHandler;
import org.zcx.netty.mqtt.TopicUtil;
import org.zcx.netty.mqtt.dto.MyMqttMessage;
import org.zcx.netty.handler.abstractHandler.AbstractMqttClientHandler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ChannelHandler.Sharable
public class MultiTopicMqttClientHandler extends AbstractMqttClientHandler implements DynamicHandler, ConfigurableBean {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    //一个topic对应多个handler
    private Map<String, Map<String, MqttTopicHandler>> topicHandlerMap = new HashMap<>();

    private String host;
    private Integer port;
    private String userName;
    private String password;

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

    public void sendMsg1(String topicName, String msg) {
        sendMsg0(getChannelId(), topicName, msg);
    }

    public void subscribe0(String topicName, String topicHandlerName) {
        MqttTopicHandler topicHandler = SpringUtils.getBean(topicHandlerName, MqttTopicHandler.class);
        if (topicHandler == null){
            throw new HandlerException("mqtt消息处理类不存在");
        }
        Map<String, MqttTopicHandler> oneTopicHandlerMap = topicHandlerMap.computeIfAbsent(topicName, k -> new HashMap<>());
        oneTopicHandlerMap.put(topicHandlerName, topicHandler);
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
                Map<String, MqttTopicHandler> oneTopicHandlerMap = topicHandlerMap.get(topic);
                if (oneTopicHandlerMap != null && !oneTopicHandlerMap.isEmpty()) {
                    for (MqttTopicHandler topicHandler : oneTopicHandlerMap.values()) {
                        topicHandler.handleMassage(fullTopic, message);
                    }
                }
            }
        }
    }

    //todo 是否只有一个连接？
    private String getChannelId() {
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

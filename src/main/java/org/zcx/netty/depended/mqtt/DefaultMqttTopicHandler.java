package org.zcx.netty.depended.mqtt;

import org.springframework.stereotype.Component;

@Component("defaultMqttTopicHandler")
public class DefaultMqttTopicHandler implements MqttTopicHandler {

    @Override
    public Object handleMassage(String fullTopic,String msg) {
        System.out.println("defaultMqttTopicHandler 处理消息"+msg);
        return null;
    }
}

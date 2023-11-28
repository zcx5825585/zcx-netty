package org.zcx.netty.mqtt.topicHandler;

import org.springframework.stereotype.Component;
import org.zcx.netty.bean.ClassRegisterInfo;
import org.zcx.netty.mqtt.topicHandler.MqttTopicHandler;
import org.zcx.netty.mqtt.topicHandler.TopicHandlerManager;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component("defaultMqttTopicHandler")
public class DefaultMqttTopicHandler implements MqttTopicHandler {

    @Resource
    private TopicHandlerManager manager;

    @Override
    public Object handleMassage(String fullTopic,String msg) {
        System.out.println("defaultMqttTopicHandler 处理消息"+msg);
        return null;
    }

    @PostConstruct
    public void init(){
        ClassRegisterInfo registerInfo = new ClassRegisterInfo();
        registerInfo.setBeanName("defaultMqttTopicHandler");
        manager.registerHandler(registerInfo);
    }
}

package org.zcx.netty.web.service;

import org.springframework.stereotype.Service;
import org.zcx.netty.common.DynamicHandler;
import org.zcx.netty.common.bean.ClassRegisterInfo;
import org.zcx.netty.depended.mqtt.MqttTopicHandler;
import org.zcx.netty.depended.mqtt.TopicHandlerManager;
import org.zcx.netty.handler.configurableHandler.MultiTopicMqttClientHandler;

import javax.annotation.Resource;
import java.util.List;

@Service
public class MqttService {

    @Resource
    private TopicHandlerManager topicHandlerManager;
    @Resource
    private HandlerService handlerService;

    public String subscribe(Long handlerId, String topicName, String topicHandlerName) {
        DynamicHandler handler = handlerService.getById(handlerId).getHandler();
        MultiTopicMqttClientHandler mqttClientHandler = (MultiTopicMqttClientHandler) handler;
        mqttClientHandler.subscribe0(topicName, topicHandlerName);
        return "success";
    }

    public List<String> getTopicHandlerList() {
        return topicHandlerManager.getAllTopicHandler();
    }

    public void sendMqttMsg(Long handlerId, String topic, String msg) {
        DynamicHandler handler = handlerService.getById(handlerId).getHandler();
        MultiTopicMqttClientHandler mqttClientHandler = (MultiTopicMqttClientHandler) handler;
        mqttClientHandler.sendMsg1(topic, msg);
    }

    public void register(String topicHandlerName) {
        ClassRegisterInfo registerInfo = new ClassRegisterInfo();
        registerInfo.setBeanName(topicHandlerName);
        registerInfo.setLoaderType("fileLoader");
        registerInfo.setPackageName("dynamicBean.mqttTopicHandler");
        topicHandlerManager.registerHandler(registerInfo);
    }

    public void register(String topicHandlerName, String javaStr) {
        ClassRegisterInfo registerInfo = new ClassRegisterInfo();
        registerInfo.setBeanName(topicHandlerName);
        registerInfo.setLoaderType("scriptMemoryLoader");
        registerInfo.setPackageName("dynamicBean.mqttTopicHandler");
        registerInfo.setJavaSrc(javaStr);
        topicHandlerManager.registerHandler(registerInfo);
    }
}

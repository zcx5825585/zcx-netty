package org.zcx.netty.mqtt.topicHandler;

import org.springframework.stereotype.Component;
import org.zcx.netty.bean.ClassRegisterInfo;
import org.zcx.netty.bean.ClassRegisterService;
import org.zcx.netty.common.utils.SpringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TopicHandlerManager {
    @Resource
    private ClassRegisterService beanRegisterService;

    private static final Map<String, MqttTopicHandler> handlerMap = new HashMap<>();

    public static MqttTopicHandler getMqttTopicHandler(String handlerName) {
        MqttTopicHandler handler = handlerMap.get(handlerName);
        return handler;
    }
    public List<String> getAllTopicHandler(){
        return new ArrayList<>(handlerMap.keySet());
    }

    public MqttTopicHandler registerHandler(ClassRegisterInfo classRegisterInfo) {
        String handlerName = classRegisterInfo.getBeanName();
        //初始化handler bean
        //已注册到spring
        MqttTopicHandler handler = handlerMap.get(handlerName);
        if (handler == null) {//打包并注册为bean
            beanRegisterService.registerBean(classRegisterInfo);
            handler = SpringUtils.getBean(handlerName, MqttTopicHandler.class);
            handlerMap.put(handlerName, handler);
        }
        return handler;
    }


}

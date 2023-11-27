package org.zcx.netty.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zcx.netty.common.DynamicHandler;
import org.zcx.netty.handler.abstractHandler.AbstractMqttClientHandler;
import org.zcx.netty.handler.configurableHandler.SingletonMqttClientHandler;
import org.zcx.netty.web.service.HandlerService;

import javax.annotation.Resource;

/**
 * @author zcx
 * @date 2023-11-13
 */
@RestController
@RequestMapping("mqtt")
public class MqttController {

    @Resource
    private HandlerService handlerService;

    @GetMapping("subscribe")
    public String getAllTcpServerHandler(Long handlerId,String topicName, String topicHandlerName) {
        DynamicHandler handler = handlerService.getById(handlerId).getHandler();
        SingletonMqttClientHandler mqttClientHandler = (SingletonMqttClientHandler) handler;
        mqttClientHandler.subscribe0("zcx/#","defaultMqttTopicHandler");
        return "success";
    }


    @GetMapping("sendMqttMsg")
    public String sendMqttMsg(Long handlerId, String channelId, String topic, String msg) throws Exception {
        DynamicHandler writableHandler = handlerService.getById(handlerId).getHandler();
        if (writableHandler instanceof AbstractMqttClientHandler) {
            AbstractMqttClientHandler mqttClientHandler = (AbstractMqttClientHandler) writableHandler;
            mqttClientHandler.sendMsg0(channelId, topic, msg);
        }
        return "success";
    }
}

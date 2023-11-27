package org.zcx.netty.web.controller;

import org.springframework.web.bind.annotation.*;
import org.zcx.netty.web.service.MqttService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zcx
 * @date 2023-11-27
 */
@RestController
@RequestMapping("mqtt")
public class MqttController {
    @Resource
    private MqttService mqttService;

    @GetMapping("getTopicHandlerList")
    public List<String> getTopicHandlerList() {
        return mqttService.getTopicHandlerList();
    }

    @GetMapping("registerByFile")
    public String registerByFile(String topicHandlerName) {
        mqttService.register(topicHandlerName);
        return topicHandlerName;
    }

    @PostMapping("registerByJavaStr")
    public String registerByJavaStr(@RequestParam String topicHandlerName,@RequestBody String javaStr) {
        mqttService.register(topicHandlerName,javaStr);
        return topicHandlerName;
    }

    @GetMapping("subscribe")
    public String subscribe(Long handlerId, String topicName, String topicHandlerName) {
        mqttService.subscribe(handlerId, topicName, topicHandlerName);
        return "success";
    }

    @GetMapping("sendMqttMsg")
    public String sendMqttMsg(Long handlerId, String topic, String msg) throws Exception {
        mqttService.sendMqttMsg(handlerId, topic, msg);
        return "success";
    }
}

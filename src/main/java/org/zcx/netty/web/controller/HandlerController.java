package org.zcx.netty.web.controller;

import org.springframework.web.bind.annotation.*;
import org.zcx.netty.handler.DynamicHandler;
import org.zcx.netty.handler.abstractHandler.AbstractMqttClientHandler;
import org.zcx.netty.web.entity.HandlerInfo;
import org.zcx.netty.web.service.HandlerService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zcx
 * @date 2023-11-13
 */
@RestController
@RequestMapping("handler")
public class HandlerController {

    @Resource
    private HandlerService handlerService;

    @GetMapping("list")
    public List<HandlerInfo> getAllTcpServerHandler(Long groupId) {
        return handlerService.getHandlersByGroup(groupId);
    }


    @PostMapping("add")
    public String add(@RequestBody HandlerInfo handlerInfo) throws Exception {
        handlerService.add(handlerInfo);
        return "success";
    }

    @PostMapping("register")
    public DynamicHandler register(Long id) throws Exception {
        return handlerService.register(id);
    }

    @GetMapping("serverStart")
    public String serverStart(Long handlerId, Integer port) throws Exception {
        handlerService.serverStart(handlerId, port);
        return "success";
    }

    @GetMapping("connect")
    public String connect(Long handlerId) throws Exception {
        handlerService.connect(handlerId);
        return "success";
    }

    @GetMapping("disconnect")
    public String disconnect(Long handlerId, String channelId) throws Exception {
        DynamicHandler writableHandler = handlerService.getById(handlerId).getHandler();
        writableHandler.disconnect(channelId);
        return "success";
    }

    @GetMapping("sendMsg")
    public String sendMsg(Long handlerId, String channelId, String msg) throws Exception {
        DynamicHandler writableHandler = handlerService.getById(handlerId).getHandler();
        writableHandler.sendMsg(channelId, msg);
        return "success";
    }

    @GetMapping("getChannelList")
    public List<String> getChannelList(Long handlerId) throws Exception {
        DynamicHandler writableHandler = handlerService.getById(handlerId).getHandler();
        return writableHandler.getChannelList();
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

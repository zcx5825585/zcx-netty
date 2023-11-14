package org.zcx.netty.controller;

import org.springframework.web.bind.annotation.*;
import org.zcx.netty.bean.HandlerManager;
import org.zcx.netty.handler.WritableHandler;

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
    private HandlerManager handlerManager;

    @GetMapping("list")
    public List<String> getAllHandler() {
        return handlerManager.getAllHandlerNames();
    }


    @PostMapping("register")
    public String register(String handlerName) throws Exception {
        boolean result=handlerManager.registerHandler(handlerName);
        return result?"success":"fail";
    }

    @GetMapping("sendMsg")
    public String sendMsg(String handlerName,String channelId,String msg) throws Exception {
        WritableHandler writableHandler = handlerManager.getWritableHandler(handlerName);
        writableHandler.sendMsg(channelId,msg);
        return "success";
    }

    @GetMapping("getChannelList")
    public List<String> getChannelList(String handlerName) throws Exception {
        WritableHandler writableHandler = handlerManager.getWritableHandler(handlerName);
        return writableHandler.getChannelList();
    }
}

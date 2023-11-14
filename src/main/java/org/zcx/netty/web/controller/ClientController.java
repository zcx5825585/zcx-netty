package org.zcx.netty.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zcx.netty.common.DynamicHandler;
import org.zcx.netty.web.entity.HandlerInfo;
import org.zcx.netty.web.service.ClientService;

import javax.annotation.Resource;

/**
 * @author zcx
 * @date 2023-11-13
 */
@RestController
@RequestMapping("client")
public class ClientController {
    @Resource
    private ClientService clientService;

    @GetMapping("run")
    public String run(Long clientId) throws Exception {
        clientService.run(clientId);
        return "success";
    }

    @GetMapping("getHandler")
    public HandlerInfo getHandler(Long clientId) throws Exception {
        return clientService.getHandler(clientId);
    }

}

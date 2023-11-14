package org.zcx.netty.web.service;

import org.springframework.stereotype.Service;
import org.zcx.netty.client.NettyClientRunner;
import org.zcx.netty.common.DynamicHandler;
import org.zcx.netty.web.dao.ClientDao;
import org.zcx.netty.web.entity.ClientInfo;
import org.zcx.netty.web.entity.HandlerInfo;

import javax.annotation.Resource;

@Service
public class ClientService {
    @Resource
    private NettyClientRunner clientRunner;
    @Resource
    private HandlerService handlerService;
    @Resource
    private ClientDao clientDao;

    public void run(Long clientId) {
        ClientInfo clientInfo = clientDao.getById(clientId);
        DynamicHandler handler = handlerService.getById(clientInfo.getHandlerId()).getHandler();
        clientRunner.runHandlerAsClient(clientInfo.getIp(), clientInfo.getPort(), handler);
    }


    public HandlerInfo getHandler(Long clientId) {
        ClientInfo clientInfo = clientDao.getById(clientId);
        HandlerInfo handler = handlerService.getById(clientInfo.getHandlerId());
        return handler;
    }
}

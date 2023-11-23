package org.zcx.netty.web.service;

import org.springframework.stereotype.Service;
import org.zcx.netty.bootstrap.NettyTcpClientRunner;
import org.zcx.netty.bootstrap.NettyTcpServer;
import org.zcx.netty.common.DynamicHandler;
import org.zcx.netty.common.HandlerManager;
import org.zcx.netty.common.bean.ClassRegisterInfo;
import org.zcx.netty.common.exception.HandlerException;
import org.zcx.netty.web.dao.HandlerInfoDao;
import org.zcx.netty.web.entity.HandlerInfo;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.net.BindException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HandlerService {
    @Resource
    private NettyTcpServer serverRunner;
    @Resource
    private NettyTcpClientRunner clientRunner;
    @Resource
    private HandlerManager handlerManager;
    @Resource
    private HandlerInfoDao handlerDao;

    @PostConstruct
    public void init() {
        add(new HandlerInfo(1L, "httpHandler", 1L));
        add(new HandlerInfo(2L, "tcpHandler", 1L));
//        add(new HandlerInfo(3L, "wsHandler", 1L));
//        add(new HandlerInfo(4L, "ws2Handler", 1L));
//        add(new HandlerInfo(5L, "http2Handler", 1L));
        add(new HandlerInfo(6L, "tcpClientHandler", 2L));
        add(new HandlerInfo(7L, "mqttClientHandler", 2L));
        HandlerInfo configMqttClientHandler = new HandlerInfo(8L, "configMqttClientHandler", 2L);
        configMqttClientHandler.setBaseHandlerName("mqttClientHandler");
        Map<String, Object> params = new HashMap<>();
        params.put("defaultTopic", "zcx/#");
        params.put("userName", "smartsite");
        params.put("password", "smartsite12347988");
        configMqttClientHandler.setArgs(params);
        add(configMqttClientHandler);

        handlerDao.setCurrentId(9L);

        //初始化handler
        List<HandlerInfo> handlerInfos = handlerDao.list(null);
        handlerInfos.stream().filter(HandlerInfo::getAutoRegister).forEach(one -> {
            register(one.getId());
        });

    }

    public HandlerInfo getById(Long id) {
        HandlerInfo handlerInfo = handlerDao.getById(id);
        handlerInfo.setHandler(HandlerManager.getDynamicHandler(handlerInfo.getHandlerName()));
        return handlerInfo;
    }

    public List<HandlerInfo> getHandlersByGroup(Long groupId) {
        HandlerInfo query = new HandlerInfo();
        query.setGroupId(groupId);
        List<HandlerInfo> handlerInfos = handlerDao.list(query);
        return handlerInfos.stream().peek(one -> {
            try {
                one.setHandler(HandlerManager.getDynamicHandler(one.getHandlerName()));
            } catch (Exception e) {
                one.setHandler(null);
            }
        }).collect(Collectors.toList());
    }

    public void add(HandlerInfo handlerInfo) {
        handlerDao.add(handlerInfo);
    }

    public DynamicHandler register(Long id) {
        HandlerInfo handlerInfo = handlerDao.getById(id);
        ClassRegisterInfo classRegisterInfo = new ClassRegisterInfo();
        classRegisterInfo.setBeanName(handlerInfo.getHandlerName());
        classRegisterInfo.setBaseBeanName(handlerInfo.getBaseHandlerName());
        classRegisterInfo.setPackageName(handlerInfo.getPackageName());
        classRegisterInfo.setArgs(handlerInfo.getArgs());
        classRegisterInfo.setReCompiler(false);
        classRegisterInfo.setSpringBean(true);
        return handlerManager.registerHandler(classRegisterInfo);
    }

    public void connect(Long handlerId, String host, Integer port) {
        HandlerInfo handlerInfo = getById(handlerId);
        DynamicHandler handler = handlerInfo.getHandler();
        if (handler == null) {
            throw new HandlerException("handler未初始化");
        }
        clientRunner.runHandlerAsClient(host, port, handler);
    }

    public void serverStart(Long handlerId,Integer port) throws Exception {
        HandlerInfo handlerInfo = getById(handlerId);
        DynamicHandler handler = handlerInfo.getHandler();
        if (handler == null) {
            throw new HandlerException("handler未初始化");
        }
        try {
            serverRunner.runHandlerAsServer(port, handler);
        }catch (BindException e){
            throw new HandlerException("端口已占用");
        }
    }
}

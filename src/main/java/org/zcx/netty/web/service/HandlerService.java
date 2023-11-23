package org.zcx.netty.web.service;

import org.springframework.stereotype.Service;
import org.zcx.netty.bootstrap.NettyClientRunner;
import org.zcx.netty.common.DynamicHandler;
import org.zcx.netty.common.HandlerManager;
import org.zcx.netty.common.bean.ClassRegisterInfo;
import org.zcx.netty.common.exception.HandlerException;
import org.zcx.netty.web.dao.HandlerInfoDao;
import org.zcx.netty.web.entity.HandlerInfo;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HandlerService {
    @Resource
    private NettyClientRunner clientRunner;
    @Resource
    private HandlerManager handlerManager;
    @Resource
    private HandlerInfoDao handlerDao;


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
}

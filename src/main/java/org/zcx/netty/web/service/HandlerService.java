package org.zcx.netty.web.service;

import org.springframework.stereotype.Service;
import org.zcx.netty.common.HandlerManager;
import org.zcx.netty.web.dao.HandlerInfoDao;
import org.zcx.netty.web.entity.HandlerInfo;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HandlerService {
    @Resource
    private HandlerManager handlerManager;
    @Resource
    private HandlerInfoDao handlerDao;

    public HandlerInfo getById(Long id) {
        HandlerInfo handlerInfo = handlerDao.getById(id);
        handlerInfo.setHandler(handlerManager.getDynamicHandler(handlerInfo.getHandlerName()));
        return handlerInfo;
    }

    public List<HandlerInfo> getHandlersByGroup(Long groupId) {
        HandlerInfo query = new HandlerInfo();
        query.setGroupId(groupId);
        List<HandlerInfo> handlerInfos = handlerDao.list(query);
        return handlerInfos.stream().peek(one -> {
            try {
                one.setHandler(handlerManager.getDynamicHandler(one.getHandlerName()));
            } catch (Exception e) {
                one.setHandler(null);
            }
        }).collect(Collectors.toList());
    }

    public void add(HandlerInfo handlerInfo) {
        handlerDao.add(handlerInfo);
    }

    public void register(Long id) {
        HandlerInfo handlerInfo = handlerDao.getById(id);
        handlerManager.registerHandler(handlerInfo.getHandlerName(), handlerInfo.getPackageName());
    }
}

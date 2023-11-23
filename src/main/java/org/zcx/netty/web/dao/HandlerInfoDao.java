package org.zcx.netty.web.dao;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.zcx.netty.common.HandlerManager;
import org.zcx.netty.web.entity.HandlerInfo;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@DependsOn({"handlerGroupDao"})
public class HandlerInfoDao {
    @Resource
    private HandlerManager handlerManager;
    @Resource
    private HandlerGroupDao groupDao;

    private Long currentId = 1L;

    public void setCurrentId(long id) {
        this.currentId = id;
    }


    private Map<Long, HandlerInfo> map = new HashMap<>();


    public List<HandlerInfo> list(HandlerInfo query) {
        return map.values().stream().filter(one -> {
            if (query == null) {
                return true;
            }
            if (query.getHandlerName() != null && !one.getHandlerName().contains(query.getHandlerName())) {
                return false;
            }
            if (query.getGroupId() != null && one.getGroupId() != query.getGroupId()) {
                return false;
            }
            return true;
        }).collect(Collectors.toList());
    }

    public HandlerInfo getById(Long id) {
        return map.get(id);
    }

    public HandlerInfo getByName(String name) {
        for (HandlerInfo handlerInfo : map.values()) {
            if (name.equals(handlerInfo.getHandlerName())) {
                return handlerInfo;
            }
        }
        return null;
    }

    public void add(HandlerInfo handlerInfo) {
        if (handlerInfo.getId() == null) {//方便固定测试数据的ID
            handlerInfo.setId(currentId);
        }
        handlerInfo.setPackageName(groupDao.getById(handlerInfo.getGroupId()).getPackageName());
        map.put(handlerInfo.getId(), handlerInfo);
        currentId++;
    }

    public void update(HandlerInfo handlerInfo) {
        map.put(handlerInfo.getId(), handlerInfo);
    }

    public void delete(Long id) {
        map.remove(id);
    }

    public HandlerInfo getBySn(String sn) {
        return getByName(sn);
    }
}

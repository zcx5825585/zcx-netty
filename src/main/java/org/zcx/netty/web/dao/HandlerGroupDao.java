package org.zcx.netty.web.dao;

import org.springframework.stereotype.Component;
import org.zcx.netty.web.entity.HandlerGroup;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class HandlerGroupDao {
    private Long currentId = 1L;

    private Map<Long, HandlerGroup> map = new HashMap<>();

    @PostConstruct
    public void init() {
        HandlerGroup tcpServerGroup = new HandlerGroup();
        tcpServerGroup.setGroupId(1L);
        tcpServerGroup.setGroupName("tcpServerGroup");
        tcpServerGroup.setPackageName("dynamicBean.tcpServerHandler");
        this.add(tcpServerGroup);

        HandlerGroup tcpClientGroup = new HandlerGroup();
        tcpClientGroup.setGroupId(2L);
        tcpClientGroup.setGroupName("tcpClientGroup");
        tcpClientGroup.setPackageName("dynamicBean.tcpClientHandler");
        this.add(tcpClientGroup);
        this.currentId = 3L;
    }

    public List<HandlerGroup> list(HandlerGroup query) {
        return map.values().stream().filter(one -> {
            if (query.getGroupName() != null && !one.getGroupName().equals(query.getGroupName())) {
                return false;
            }
            return true;
        }).collect(Collectors.toList());
    }

    public HandlerGroup getById(Long id) {
        return map.get(id);
    }

    public void add(HandlerGroup handlerGroup) {
        if (handlerGroup.getGroupId() == null) {//方便固定测试数据的ID
            handlerGroup.setGroupId(currentId);
        }
        map.put(currentId, handlerGroup);
        currentId++;
    }

    public void update(HandlerGroup handlerGroup) {
        map.put(handlerGroup.getGroupId(), handlerGroup);
    }

    public void delete(Long id) {
        map.remove(id);
    }

}

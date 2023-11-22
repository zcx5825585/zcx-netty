package org.zcx.netty.web.dao;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.zcx.netty.common.HandlerManager;
import org.zcx.netty.common.bean.ClassRegisterInfo;
import org.zcx.netty.web.entity.HandlerInfo;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Arrays;
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


    private Map<Long, HandlerInfo> map = new HashMap<>();

    @PostConstruct
    public void init() {
//        this.add(new HandlerInfo(1L, "httpHandler", 1L));
//        this.add(new HandlerInfo(2L, "tcpHandler", 1L));
//        this.add(new HandlerInfo(3L, "wsHandler", 1L));
//        this.add(new HandlerInfo(4L, "ws2Handler", 1L));
//        this.add(new HandlerInfo(5L, "http2Handler", 1L));
//        this.add(new HandlerInfo(6L, "tcpClientHandler", 2L));
        this.add(new HandlerInfo(7L, "mqttClientHandler", 2L));
        HandlerInfo configMqttClientHandler = new HandlerInfo(8L, "configMqttClientHandler", 2L);
        configMqttClientHandler.setBaseHandlerName("mqttClientHandler");
        Map<String,Object> params = new HashMap<>();
        params.put("defaultTopic", "zcx/#");
        params.put("userName","smartsite");
        params.put("password","smartsite12347988");
        configMqttClientHandler.setArgs(params);
        this.add(configMqttClientHandler);
        this.currentId = 9L;

        //初始化handler
        List<HandlerInfo> handlerInfos = list(null);
        handlerInfos.forEach(one -> {
            one.setGroup(groupDao.getById(one.getGroupId()));

            ClassRegisterInfo classRegisterInfo = new ClassRegisterInfo();
            classRegisterInfo.setBeanName(one.getHandlerName());
            classRegisterInfo.setBaseBeanName(one.getBaseHandlerName());
            classRegisterInfo.setPackageName(one.getPackageName());
            classRegisterInfo.setArgs(one.getArgs());
            classRegisterInfo.setReCompiler(false);
            classRegisterInfo.setSpringBean(true);
            handlerManager.registerHandler(classRegisterInfo);
        });

    }

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
        handlerInfo.setGroup(groupDao.getById(handlerInfo.getGroupId()));
        map.put(handlerInfo.getId(), handlerInfo);
        currentId++;
    }

    public void update(HandlerInfo handlerInfo) {
        map.put(handlerInfo.getId(), handlerInfo);
    }

    public void delete(Long id) {
        map.remove(id);
    }

}

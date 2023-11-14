package org.zcx.netty.web.dao;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.zcx.netty.web.entity.ClientInfo;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@DependsOn({"handlerInfoDao"})
public class ClientDao {
    private Long currentId = 1L;

    private Map<Long, ClientInfo> map = new HashMap<>();

    @PostConstruct
    public void init() {
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setId(1L);
        clientInfo.setIp("127.0.0.1");
        clientInfo.setPort(18021);
        clientInfo.setHandlerId(6L);
        this.add(clientInfo);

        ClientInfo clientInfo2 = new ClientInfo();
        clientInfo2.setId(2L);
        clientInfo2.setIp("47.105.217.47");
        clientInfo2.setPort(1883);
        clientInfo2.setHandlerId(7L);
        this.add(clientInfo2);

        this.currentId = 3L;
    }

    public List<ClientInfo> list(ClientInfo query) {
        return map.values().stream().filter(one -> {
            return true;
        }).collect(Collectors.toList());
    }

    public ClientInfo getById(Long id) {
        return map.get(id);
    }

    public void add(ClientInfo clientInfo) {
        if (clientInfo.getId() == null) {//方便固定测试数据的ID
            clientInfo.setId(currentId);
        }
        map.put(currentId, clientInfo);
        currentId++;
    }

    public void update(ClientInfo clientInfo) {
        map.put(clientInfo.getId(), clientInfo);
    }

    public void delete(Long id) {
        map.remove(id);
    }

}

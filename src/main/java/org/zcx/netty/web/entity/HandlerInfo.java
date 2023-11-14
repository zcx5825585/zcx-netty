package org.zcx.netty.web.entity;

import org.zcx.netty.common.DynamicHandler;

public class HandlerInfo {
    private Long id;
    private String handlerName;
    private Long groupId;
    private HandlerGroup group;
    private DynamicHandler handler;

    public HandlerInfo() {
    }

    public HandlerInfo(Long id, String handlerName, Long groupId) {
        this.id = id;
        this.handlerName = handlerName;
        this.groupId = groupId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public HandlerGroup getGroup() {
        return group;
    }

    public void setGroup(HandlerGroup group) {
        this.group = group;
    }

    public DynamicHandler getHandler() {
        return handler;
    }

    public void setHandler(DynamicHandler handler) {
        this.handler = handler;
    }

    public String getPackageName() {
        return group.getPackageName();
    }

}

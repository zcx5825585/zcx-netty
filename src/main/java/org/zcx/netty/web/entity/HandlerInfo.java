package org.zcx.netty.web.entity;

import org.zcx.netty.common.DynamicHandler;

import java.util.Map;

public class HandlerInfo {
    private Long id;
    private String loaderType = "fileLoader";
    private String javaSrc;
    private String handlerName;
    private String baseHandlerName;
    private Long groupId;
    private Long classId;
    private String version;
    private String packageName;
    private Boolean autoRegister = true;
    private Boolean isRunning = false;
    private Map<String, Object> args;
    private DynamicHandler handler;

    public HandlerInfo() {
    }

    public HandlerInfo(Long id, String handlerName, Long groupId) {
        this.id = id;
        this.handlerName = handlerName;
        this.groupId = groupId;
    }

    public String getLoaderType() {
        return loaderType;
    }

    public void setLoaderType(String loaderType) {
        this.loaderType = loaderType;
    }

    public String getJavaSrc() {
        return javaSrc;
    }

    public void setJavaSrc(String javaSrc) {
        this.javaSrc = javaSrc;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public DynamicHandler getHandler() {
        return handler;
    }

    public void setHandler(DynamicHandler handler) {
        this.handler = handler;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public String getBaseHandlerName() {
        if (baseHandlerName == null) {
            return handlerName;
        }
        return baseHandlerName;
    }

    public void setBaseHandlerName(String baseHandlerName) {
        this.baseHandlerName = baseHandlerName;
    }

    public Map<String, Object> getArgs() {
        return args;
    }

    public void setArgs(Map<String, Object> args) {
        this.args = args;
    }

    public Boolean getRunning() {
        return isRunning;
    }

    public void setRunning(Boolean running) {
        isRunning = running;
    }

    public Boolean getAutoRegister() {
        return autoRegister;
    }

    public void setAutoRegister(Boolean autoRegister) {
        this.autoRegister = autoRegister;
    }
}

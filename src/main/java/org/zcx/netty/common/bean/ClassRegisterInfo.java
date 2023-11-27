package org.zcx.netty.common.bean;

import java.util.*;

public class ClassRegisterInfo {
    //加载方式
    //fileLoader 以文件方式加载 必需字段 packageName
    //scriptMemoryLoader 以文本方式加载 必需字段 packageName javaSrc
    //configurableBean 参数配置 ConfigurableBean的实现类 必需字段 baseBeanName args
    private String loaderType;
    //包名
    private String packageName;
    //bean名称
    private String beanName;
    //要配置的ConfigurableBean的实现类
    private String baseBeanName;
    //配置参数
    private Map<String, Object> args;
    //java文本
    private String javaSrc;
    //是否重新编译
    private boolean reCompiler = false;
    //是否注册为bean
    private boolean springBean = true;
    //需要注册的依赖
    private List<ClassRegisterInfo> dependClass = Collections.emptyList();

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

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getBaseBeanName() {
        return baseBeanName;
    }

    public void setBaseBeanName(String baseBeanName) {
        this.baseBeanName = baseBeanName;
    }

    public String getClassName() {
        return beanName.substring(0, 1).toUpperCase(Locale.ROOT) + beanName.substring(1);
    }

    public boolean isReCompiler() {
        return reCompiler;
    }

    public void setReCompiler(boolean reCompiler) {
        this.reCompiler = reCompiler;
    }

    public boolean isSpringBean() {
        return springBean;
    }

    public void setSpringBean(boolean springBean) {
        this.springBean = springBean;
    }

    public List<ClassRegisterInfo> getDependClass() {
        return dependClass;
    }

    public void setDependClass(List<ClassRegisterInfo> dependClass) {
        this.dependClass = dependClass;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Map<String, Object> getArgs() {
        return args;
    }

    public void setArgs(Map<String, Object> args) {
        this.args = args;
    }

}

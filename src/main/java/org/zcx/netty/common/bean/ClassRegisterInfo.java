package org.zcx.netty.common.bean;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ClassRegisterInfo {
    private String packageName;
    private String beanName;
    private String baseBeanName;
    private Map<String, Object> args;
    private boolean reCompiler = false;
    private boolean springBean = true;
    private List<ClassRegisterInfo> dependClass = Collections.emptyList();


    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getBaseBeanName() {
        if (baseBeanName == null) {
            return beanName;
        }
        return baseBeanName;
    }

    public void setBaseBeanName(String baseBeanName) {
        this.baseBeanName = baseBeanName;
    }

    public String getClassName() {
        return getBaseBeanName().substring(0, 1).toUpperCase(Locale.ROOT) + getBaseBeanName().substring(1);
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

package org.zcx.netty.common.bean;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ClassRegisterInfo {
    private String packageName;
    private String beanName;
    private boolean reCompiler = false;
    private boolean springBean = true;
    private List<ClassRegisterInfo> dependClass = Collections.emptyList();


    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getClassName() {
        return this.beanName.substring(0, 1).toUpperCase(Locale.ROOT) + this.beanName.substring(1);
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


}

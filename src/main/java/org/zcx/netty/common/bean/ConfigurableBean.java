package org.zcx.netty.common.bean;

import java.util.List;
import java.util.Map;

public interface ConfigurableBean {

    public void setBeanName(String beanName);

    public void config(Map<String, Object> param);

    public List<BeanParam> getParamList();
}

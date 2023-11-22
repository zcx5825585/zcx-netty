package org.zcx.netty.common;

import org.springframework.stereotype.Component;
import org.zcx.netty.common.bean.ClassRegisterInfo;
import org.zcx.netty.common.bean.ClassRegisterService;
import org.zcx.netty.common.bean.ConfigurableBean;
import org.zcx.netty.common.utils.SpringUtils;

import javax.annotation.Resource;
import java.util.Map;

@Component
public class HandlerManager {
    @Resource
    private ClassRegisterService beanRegisterService;
    @Resource
    private Map<String, DynamicHandler> handlerMap;


    public DynamicHandler getDynamicHandler(String handlerName) {
        DynamicHandler handler = handlerMap.get(handlerName);
        return handler;
    }


    public DynamicHandler registerHandler(ClassRegisterInfo classRegisterInfo) {
        String handlerName = classRegisterInfo.getBeanName();
        //初始化handler bean
        //已注册到spring
        DynamicHandler handler = handlerMap.get(handlerName);
        if (handler == null) {//打包并注册为bean
            beanRegisterService.registerBean(classRegisterInfo);
            handler = SpringUtils.getBean(handlerName, DynamicHandler.class);
            handlerMap.put(handlerName, handler);
        }
        if (handler instanceof ConfigurableBean){
            ConfigurableBean configurableBean=(ConfigurableBean)handler;
            if (!configurableBean.isConfigured()){
                handlerMap.remove(handlerName);
            }
        }
        return handler;
    }


}

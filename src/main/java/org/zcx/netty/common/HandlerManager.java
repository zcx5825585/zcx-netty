package org.zcx.netty.common;

import org.springframework.stereotype.Component;
import org.zcx.netty.common.bean.ClassRegisterInfo;
import org.zcx.netty.common.bean.ClassRegisterService;
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
        DynamicHandler handler = SpringUtils.getBean(handlerName, DynamicHandler.class);
        return handler;
    }


    public DynamicHandler registerHandler(String handlerName, String packageName) {
        //初始化handler bean
        //已注册到spring
        DynamicHandler handler = handlerMap.get(handlerName);
        if (handler == null) {//打包并注册为bean
            ClassRegisterInfo classRegisterInfo = new ClassRegisterInfo();
            classRegisterInfo.setBeanName(handlerName);
            classRegisterInfo.setPackageName(packageName);
            classRegisterInfo.setReCompiler(false);
            classRegisterInfo.setSpringBean(true);
            beanRegisterService.registerBean(classRegisterInfo);
            handler = SpringUtils.getBean(handlerName, DynamicHandler.class);
            handlerMap.put(handlerName, handler);
        }
        return handler;
    }


}

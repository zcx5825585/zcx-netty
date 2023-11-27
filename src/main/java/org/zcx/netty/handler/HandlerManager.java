package org.zcx.netty.handler;

import org.springframework.stereotype.Component;
import org.zcx.netty.bean.ClassRegisterInfo;
import org.zcx.netty.bean.ClassRegisterService;
import org.zcx.netty.common.utils.SpringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Component
public class HandlerManager {
    @Resource
    private ClassRegisterService beanRegisterService;

    private static final Map<String, DynamicHandler> handlerMap = new HashMap<>();

    public static DynamicHandler getDynamicHandler(String handlerName) {
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
//            if (handler instanceof ConfigurableBean) {//只注册为springbean 不注册为handler
//
//            } else {
                handlerMap.put(handlerName, handler);
//            }
        }
        return handler;
    }


}

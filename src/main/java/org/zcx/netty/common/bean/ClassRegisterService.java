package org.zcx.netty.common.bean;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;
import org.zcx.netty.common.exception.BeanException;

import javax.annotation.Resource;
import java.util.Map;

@Component
public class ClassRegisterService implements ApplicationContextAware {

    @Resource
    private Map<String, MyClassLoader> classLoaderMap;

    private static GenericApplicationContext genericApplicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.genericApplicationContext = (GenericApplicationContext) applicationContext;
    }

    public boolean isBeanNameInUse(String beanName) {
        return genericApplicationContext.isBeanNameInUse(beanName);
    }

    public boolean registerBean(ClassRegisterInfo registerInfo) {
        String beanName = registerInfo.getBeanName();
        if (genericApplicationContext.isBeanNameInUse(beanName)) {
            if (registerInfo.isReCompiler()) {
                genericApplicationContext.removeBeanDefinition(beanName);
            } else {
                return true;
            }
        }
        //以其他bean为基础
        if (genericApplicationContext.isBeanNameInUse(registerInfo.getBaseBeanName())) {
            Class clazz = genericApplicationContext.getType(registerInfo.getBaseBeanName());
            if (ConfigurableBean.class.isAssignableFrom(clazz)) {
                try {
                    ConfigurableBean object = (ConfigurableBean) clazz.getConstructor().newInstance();
                    BeanParam.paramsCheck(object.getParamList(), registerInfo.getArgs());
                } catch (BeanException e) {
                    throw e;
                } catch (Exception e) {
                    throw new BeanException("参数校验失败");
                }
                genericApplicationContext.registerBean(beanName, clazz);
                ConfigurableBean configurableBean = (ConfigurableBean) genericApplicationContext.getBean(beanName);
                configurableBean.setBeanName(beanName);
                configurableBean.config(registerInfo.getArgs());
                return true;
            } else {
                throw new BeanException("基础bean不可配置");
            }
        }
        for (ClassRegisterInfo dependClass : registerInfo.getDependClass()) {
            if (dependClass.isSpringBean()) {
                registerBean(dependClass);
            }
        }
        //加载class
        //加载依赖
        for (ClassRegisterInfo dependClass : registerInfo.getDependClass()) {
            if (!dependClass.isSpringBean()) {
                loadClass(dependClass);
            }
        }
        Class clazz = loadClass(registerInfo);
        //注册bean
        genericApplicationContext.registerBean(beanName, clazz);
        return true;
    }

    public Class loadClass(ClassRegisterInfo registerInfo) {
        MyClassLoader classLoader = classLoaderMap.get(registerInfo.getLoaderType());

        Class clazz = classLoader.loadClass(registerInfo);
        return clazz;
    }

}

package org.zcx.netty.common.bean;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;
import org.zcx.netty.common.exception.BeanException;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

@Component
public class ClassRegisterService implements ApplicationContextAware {

    private static ApplicationContext applicationContext;
    private static GenericApplicationContext genericApplicationContext;
    public static String rootPath = "E:\\IdeaProjects\\zcx\\netty\\src\\main\\resources\\";
    public static String dynamicPath = "dynamicBean";


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
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
        Class clazz = loadClass(registerInfo);
        //注册bean
        genericApplicationContext.registerBean(beanName, clazz);
        return true;
    }

    public Class loadClass(ClassRegisterInfo registerInfo) {
        for (ClassRegisterInfo dependClass : registerInfo.getDependClass()) {
            if (!dependClass.isSpringBean()) {
                loadClass(dependClass);
            }
        }
        compilerJava(registerInfo);
        try {
            //1、首先构建文件的目录url地址，
            URL[] urls = new URL[]{new URL("file:/" + rootPath)};
            //2、使用URLClassLoader对象的loadClass方法加载对应类
            URLClassLoader loader = new URLClassLoader(urls);
            //3、获取所加载类的方法
            Class clazz = loader.loadClass(dynamicPath + "." + registerInfo.getPackageName() + "." + registerInfo.getClassName());
            return clazz;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BeanException("载入class失败");
        }
    }

    public boolean compilerJava(ClassRegisterInfo registerInfo) {
        String classPath = rootPath + dynamicPath + File.separator + registerInfo.getPackageName() + File.separator + registerInfo.getClassName() + ".class"; //class路径
        File classFile = new File(classPath);
        if (classFile.exists()) {
            if (registerInfo.isReCompiler()) {
                classFile.delete();
                String dirPath = rootPath + dynamicPath + File.separator + registerInfo.getPackageName() + File.separator;
                File dir = new File(dirPath);
                if (dir.isDirectory()) {
                    for (File file : dir.listFiles()) {
                        if (file.getName().startsWith(registerInfo.getClassName()) && file.getName().endsWith(".class")) {
                            file.delete();
                        }
                    }
                }
            } else {
                return true;
            }
        }
        String javaPath = rootPath + dynamicPath + File.separator + registerInfo.getPackageName() + File.separator + registerInfo.getClassName() + ".java"; //路径
        File javaFile = new File(javaPath);
        if (!javaFile.exists()) {
            throw new BeanException("编译class失败：java文件未找到");
        }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler(); //调用动态编译的工具
        //进行动态编译，并返回结果
        int result = compiler.run(null, null, null, javaPath);
        if (result != 0) {
            throw new BeanException("编译class失败：编译失败");
        }
        return result == 0;
    }

}

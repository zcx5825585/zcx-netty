package org.zcx.netty.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;

@Component
public class BeanRegisterUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;
    private static GenericApplicationContext genericApplicationContext;
    public static String rootPath = "E:\\IdeaProjects\\zcx\\netty\\src\\main\\resources\\";
    public static String packageName = "dynamicBean";


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        BeanRegisterUtils.applicationContext = applicationContext;
        BeanRegisterUtils.genericApplicationContext = (GenericApplicationContext) applicationContext;
    }

    public static boolean compilerJava(String ClassName, boolean reCompiler) {
        String classPath = rootPath + packageName + "\\" + ClassName + ".class"; //class路径
        if (FileUtils.fileExists(classPath)) {
            if (reCompiler) {
                FileUtils.deleteFile(new File(classPath));
            } else {
                return true;
            }
        }
        String path = rootPath + packageName + "\\" + ClassName + ".java"; //路径
        if (!FileUtils.fileExists(path)) {
            return false;
        }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler(); //调用动态编译的工具
        //进行动态编译，并返回结果
        int result = compiler.run(null, null, null, path);
        return result == 0;
    }

    public static boolean registerBean(String beanName, boolean reCompiler) throws MalformedURLException, ClassNotFoundException {
        if (genericApplicationContext.isBeanNameInUse(beanName) && !reCompiler) {
            return true;
        }
        String ClassName = beanName.substring(0, 1).toUpperCase(Locale.ROOT) + beanName.substring(1);
        if (!compilerJava(ClassName, reCompiler)) {
            return false;
        }
        if (genericApplicationContext.isBeanNameInUse(beanName) && reCompiler) {
            genericApplicationContext.removeBeanDefinition(beanName);
        }
        //1、首先构建文件的目录url地址，
        URL[] urls = new URL[]{new URL("file:/" + rootPath)};
        //2、使用URLClassLoader对象的loadClass方法加载对应类
        URLClassLoader loder = new URLClassLoader(urls);
        //3、获取所加载类的方法
        Class clazz = loder.loadClass(packageName + "." + ClassName);
        //注册bean
        genericApplicationContext.registerBean(beanName, clazz);
        return true;
    }
}

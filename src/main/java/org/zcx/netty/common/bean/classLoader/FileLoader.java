package org.zcx.netty.common.bean.classLoader;

import org.springframework.stereotype.Component;
import org.zcx.netty.common.bean.ClassRegisterInfo;
import org.zcx.netty.common.bean.MyClassLoader;
import org.zcx.netty.common.exception.BeanException;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.regex.Matcher;

@Component("fileLoader")
public class FileLoader implements MyClassLoader {
    public static String rootPath = "E:\\IdeaProjects\\zcx\\netty\\src\\main\\resources\\";

    @Override
    public Class loadClass(ClassRegisterInfo registerInfo) {
        compilerJava(registerInfo);
        try {
            //1、首先构建文件的目录url地址，
            URL[] urls = new URL[]{new URL("file:/" + rootPath)};
            //2、使用URLClassLoader对象的loadClass方法加载对应类
            URLClassLoader loader = new URLClassLoader(urls);
            //3、获取所加载类的方法
            Class clazz = loader.loadClass(registerInfo.getPackageName() + "." + registerInfo.getClassName());
            return clazz;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BeanException("载入class失败");
        }
    }

    public boolean compilerJava(ClassRegisterInfo registerInfo) {
        //package 和 文件夹路径需保持一致 否则加载可能出现问题
        String dirPath = rootPath + registerInfo.getPackageName().replaceAll("\\.", Matcher.quoteReplacement(File.separator)) + File.separator;

        String classPath = dirPath + registerInfo.getClassName() + ".class"; //class路径
        File classFile = new File(classPath);
        if (classFile.exists()) {
            if (registerInfo.isReCompiler()) {
                classFile.delete();
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
        String javaPath = dirPath + registerInfo.getClassName() + ".java"; //路径
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

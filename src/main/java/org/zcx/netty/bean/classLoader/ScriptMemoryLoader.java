package org.zcx.netty.bean.classLoader;

import org.springframework.stereotype.Component;
import org.zcx.netty.bean.ClassRegisterInfo;
import org.zcx.netty.bean.MyClassLoader;
import org.zcx.netty.common.exception.BeanException;

import javax.annotation.PostConstruct;
import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.CharBuffer;
import java.util.*;

@Component("scriptMemoryLoader")
public class ScriptMemoryLoader implements MyClassLoader {
    protected Map<String, byte[]> classBytes = new HashMap<String, byte[]>();
    private List<MemoryClassLoader> classLoaderList;
    private MemoryJavaFileManager fileManager;

    @PostConstruct
    public void init() {
        classLoaderList = new ArrayList<>();
        MemoryClassLoader classLoader = new MemoryClassLoader();
        classLoaderList.add(classLoader);
        fileManager = new MemoryJavaFileManager(ToolProvider.getSystemJavaCompiler());
    }

    @Override
    public Class loadClass(ClassRegisterInfo registerInfo) {
        String className = registerInfo.getPackageName() + "." + registerInfo.getClassName() + ".java";
        String javaSrc = registerInfo.getJavaSrc();
        fileManager.compile(className, javaSrc);
        try {
            Class clazz = loadClass(registerInfo.getPackageName() + "." + registerInfo.getClassName());
            return clazz;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new BeanException("载入class失败");
        }
    }


    public Class<?> loadClass(String className) throws ClassNotFoundException {
        for (MemoryClassLoader classLoader : classLoaderList) {
            if (!classLoader.isClassLoaded(className)) {
                return classLoader.loadClass(className);
            }
        }
        MemoryClassLoader classLoader = new MemoryClassLoader();
        classLoaderList.add(classLoader);
        return classLoader.loadClass(className);
    }


    /**
     * 先根据类名在内存中查找是否已存在该类，若不存在则调用 URLClassLoader的 defineClass方法加载该类
     * URLClassLoader的具体作用就是将class文件加载到jvm虚拟机中去
     *
     * @author Administrator
     */
    class MemoryClassLoader extends URLClassLoader {
        public MemoryClassLoader() {
            super(new URL[0], MemoryClassLoader.class.getClassLoader());
        }

        public boolean isClassLoaded(String name) {
            return this.findLoadedClass(name) != null;
        }

        @Override
        public Class<?> findClass(String name)
                throws ClassNotFoundException {
            byte[] buf = fileManager.getClassMap().get(name);
            if (buf == null) {
                return super.findClass(name);
            }
            fileManager.getClassMap().remove(name);
            return defineClass(name, buf, 0, buf.length);
        }
    }

    /**
     * 将编译好的.class文件保存到内存当中，这里的内存也就是map映射当中
     */
    class MemoryJavaFileManager extends ForwardingJavaFileManager {

        private final static String EXT = ".java";// Java源文件的扩展名
        private Map<String, byte[]> classBytes;// 用于存放.class文件的内存
        private JavaCompiler compiler;

        public Map<String, byte[]> getClassMap(){
            return classBytes;
        }

        public MemoryJavaFileManager(JavaCompiler compiler) {
            super(compiler.getStandardFileManager(null, null, null));
            this.compiler = compiler;
            classBytes = new HashMap<String, byte[]>();
        }

        public Map<String, byte[]> getClassBytes() {
            return classBytes;
        }

        @Override
        public void close() throws IOException {
            classBytes = new HashMap<String, byte[]>();
        }

        @Override
        public void flush() throws IOException {
        }

        /**
         * 通过类名和其代码（Java代码字符串），编译得到字节码，返回类名及其对应类的字节码，封装于Map中，值得注意的是，
         * 平常类中就编译出来的字节码只有一个类，但是考虑到内部类的情况， 会出现很多个类名及其字节码，所以用Map封装方便。
         *
         * @param javaName 类名
         * @param javaSrc  Java源码
         * @return map
         */
        public void compile(String javaName, String javaSrc) {
            JavaFileObject javaFileObject = makeStringSource(javaName, javaSrc);
            JavaCompiler.CompilationTask task = compiler.getTask(null, this, null, null, null, Arrays.asList(javaFileObject));
            if (task.call()) {
                classBytes.putAll(getClassBytes());
            }
        }

        /**
         * 一个文件对象，用来表示从string中获取到的source，一下类容是按照jkd给出的例子写的
         */
        private class StringInputBuffer extends SimpleJavaFileObject {
            // The source code of this "file".
            final String code;

            /**
             * Constructs a new JavaSourceFromString.
             *
             * @param name 此文件对象表示的编译单元的name
             * @param code 此文件对象表示的编译单元source的code
             */
            StringInputBuffer(String name, String code) {
                super(toURI(name), Kind.SOURCE);
                this.code = code;
            }

            @Override
            public CharBuffer getCharContent(boolean ignoreEncodingErrors) {
                return CharBuffer.wrap(code);
            }

            public Reader openReader() {
                return new StringReader(code);
            }
        }

        /**
         * 将Java字节码存储到classBytes映射中的文件对象
         */
        private class ClassOutputBuffer extends SimpleJavaFileObject {
            private String name;

            /**
             * @param name className
             */
            ClassOutputBuffer(String name) {
                super(toURI(name), Kind.CLASS);
                this.name = name;
            }

            @Override
            public OutputStream openOutputStream() {
                return new FilterOutputStream(new ByteArrayOutputStream()) {
                    @Override
                    public void close() throws IOException {
                        out.close();
                        ByteArrayOutputStream bos = (ByteArrayOutputStream) out;

                        // 这里需要修改
                        classBytes.put(name, bos.toByteArray());
                    }
                };
            }
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            if (kind == JavaFileObject.Kind.CLASS) {
                return new ClassOutputBuffer(className);
            } else {
                return super.getJavaFileForOutput(location, className, kind, sibling);
            }
        }

        JavaFileObject makeStringSource(String name, String code) {
            return new StringInputBuffer(name, code);
        }

        URI toURI(String name) {
            File file = new File(name);
            if (file.exists()) {// 如果文件存在，返回他的URI
                return file.toURI();
            } else {
                try {
                    final StringBuilder newUri = new StringBuilder();
                    newUri.append("mfm:///");
                    newUri.append(name.replace('.', '/'));
                    if (name.endsWith(EXT)) {
                        newUri.replace(newUri.length() - EXT.length(),
                                newUri.length(), EXT);
                    }
                    return URI.create(newUri.toString());
                } catch (Exception exp) {
                    return URI.create("mfm:///com/sun/script/java/java_source");
                }
            }
        }
    }
}
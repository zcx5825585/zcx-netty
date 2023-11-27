package org.zcx.netty.bean;

import org.zcx.netty.common.exception.BeanException;

import java.util.List;
import java.util.Map;

public class BeanParam {
    private String name;
    private String title;
    private Class clazz;

    public BeanParam(String name, String title, Class clazz) {
        this.name = name;
        this.title = title;
        this.clazz = clazz;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public static void paramsCheck(List<BeanParam> params, Map<String, Object> args) {
        params.forEach(param -> {
            Object value = args.get(param.getName());
            if (value == null) {
                throw new BeanException("参数" + param.getName() + " 为空");

            }
            if (!param.clazz.isInstance(value)) {
                throw new BeanException("参数" + param.getName() + " 类型错误");
            }
        });
    }
}

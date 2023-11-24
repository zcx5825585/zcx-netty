package org.zcx.netty.script;

public class Test {
    public static void main(String[] args) {
//        lua();
        js();
    }

    public static void lua() {
        ScriptManger manger = new ScriptManger();
        ScriptDto scriptDto = new ScriptDto();
        scriptDto.setScriptType("lua");
        scriptDto.setScriptName("addScript");
//        scriptDto.setContent("function add(n1,n2)\n" +
//                "print 'hello'\n" +
//                "return n1+n2\n" +
//                "end");
        scriptDto.setFilePath("E:\\IdeaProjects\\zcx\\netty\\src\\main\\resources\\script\\lua.lua");
        scriptDto.setFunctionName("add");
        scriptDto.setParams(new Object[]{1, 2});
        manger.registerScript(scriptDto);

        Object result = manger.runScriptFunction(scriptDto);
        System.out.println(result);
    }

    public static void js() {
        ScriptManger manger = new ScriptManger();
        ScriptDto scriptDto = new ScriptDto();
        scriptDto.setScriptType("js");
        scriptDto.setScriptName("addScript");
//        scriptDto.setContent(" var fun1 = function(name) {\n" +
//                "    print('Hi there from Javascript, ' + name);\n" +
//                "    return \"greetings from javascript\";\n" +
//                "};\n" +
//                " \n" +
//                "var add = function (n1,n2) {\n" +
//                "    return n1 + n2;\n" +
//                "};");
        scriptDto.setFilePath("E:\\IdeaProjects\\zcx\\netty\\src\\main\\resources\\script\\js.js");
        scriptDto.setFunctionName("add");
        scriptDto.setParams(new Object[]{1, 2});
        manger.registerScript(scriptDto);

        Object result = manger.runScriptFunction(scriptDto);
        System.out.println(result);
    }
}

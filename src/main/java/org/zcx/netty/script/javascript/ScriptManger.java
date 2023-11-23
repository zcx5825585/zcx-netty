package org.zcx.netty.script.javascript;

import org.springframework.stereotype.Component;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;

@Component
public class ScriptManger {

    private ScriptEngineManager manager = new ScriptEngineManager();

    private Map<String, Invocable> invocableMap = new HashMap<>();

    public void registerInvocable(ScriptDto scriptDto) throws ScriptException {
        ScriptEngine jsEngine = manager.getEngineByName("js");
        jsEngine.eval(" var fun1 = function(name) {\n" +
                "    print('Hi there from Javascript, ' + name);\n" +
                "    return \"greetings from javascript\";\n" +
                "};\n" +
                " \n" +
                "var fun2 = function (object) {\n" +
                "    print(\"JS Class Definition: \" + Object.prototype.toString.call(object));\n" +
                "};");

        Invocable invocable = (Invocable) jsEngine;
        invocableMap.put(scriptDto.getScriptName(), invocable);
    }

    public Object runJSFunction(ScriptDto scriptDto) throws ScriptException, NoSuchMethodException {
        Invocable invocable = invocableMap.get(scriptDto.getScriptName());
        Object result = invocable.invokeFunction(scriptDto.getFunctionName(), scriptDto.getParams());
        return result;
    }

}

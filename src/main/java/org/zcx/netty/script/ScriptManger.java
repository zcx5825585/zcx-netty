package org.zcx.netty.script;

import org.springframework.stereotype.Component;
import org.zcx.netty.common.exception.ScriptException;
import org.zcx.netty.script.javascript.JavaScriptRunner;
import org.zcx.netty.script.lua.LuaScriptRunner;

import java.util.HashMap;
import java.util.Map;

@Component
public class ScriptManger {
    private Map<String, ScriptRunner> scriptMap = new HashMap<>();

    public void registerScript(ScriptDto scriptDto) {
        String scriptType = scriptDto.getScriptType();
        ScriptRunner scriptRunner;
        switch (scriptType) {
            case "js":
                scriptRunner = new JavaScriptRunner();
                break;
            case "lua":
                scriptRunner = new LuaScriptRunner();
                break;
            default:
                throw new ScriptException("脚本类型不支持");
        }
        scriptRunner.initScript(scriptDto);
        scriptMap.put(scriptDto.getScriptName(), scriptRunner);

    }

    public Object runScriptFunction(ScriptDto scriptDto) throws ScriptException {
        ScriptRunner scriptRunner = scriptMap.get(scriptDto.getScriptName());
        Object result = scriptRunner.runFunction(scriptDto);
        return result;
    }

}

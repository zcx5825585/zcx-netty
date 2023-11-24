package org.zcx.netty.script.lua;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.zcx.netty.common.exception.ScriptException;
import org.zcx.netty.script.ScriptDto;
import org.zcx.netty.script.ScriptRunner;

import java.util.Arrays;

public class LuaScriptRunner implements ScriptRunner {

    private Globals globals;

    @Override
    public ScriptRunner initScript(ScriptDto scriptDto) {
        this.globals = JsePlatform.standardGlobals();
        if (scriptDto.getContent() != null) {
            globals.load(scriptDto.getContent()).call();//call() 进行编译
        } else if (scriptDto.getFilePath() != null) {
            globals.loadfile(scriptDto.getFilePath()).call();//call() 进行编译
        } else {
            throw new ScriptException("脚本加载失败");
        }
        return this;
    }

    @Override
    public Object runFunction(ScriptDto scriptDto) {
        LuaValue function = globals.get(LuaValue.valueOf(scriptDto.getFunctionName()));
        LuaValue[] arr = Arrays.stream(scriptDto.getParams()).map(Object::toString).map(LuaValue::valueOf).toArray(LuaValue[]::new);
        Varargs result = function.invoke(arr);
        return result;
    }
}

package org.zcx.netty.script.javascript;

import org.zcx.netty.common.exception.ScriptException;
import org.zcx.netty.script.ScriptDto;
import org.zcx.netty.script.ScriptRunner;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class JavaScriptRunner implements ScriptRunner {

    private static ScriptEngineManager manager = new ScriptEngineManager();

    private Invocable invocable;

    @Override
    public ScriptRunner initScript(ScriptDto scriptDto) {
        ScriptEngine jsEngine = manager.getEngineByName("js");
        try {
            if (scriptDto.getContent() != null) {
                jsEngine.eval(scriptDto.getContent());
            } else if (scriptDto.getFilePath() != null) {
                jsEngine.eval(new FileReader(scriptDto.getFilePath()));
            } else {
                throw new ScriptException("脚本加载失败");
            }

            this.invocable = (Invocable) jsEngine;
        } catch (javax.script.ScriptException | FileNotFoundException e) {
            e.printStackTrace();
            throw new ScriptException(e.getMessage());
        }
        return this;
    }

    @Override
    public Object runFunction(ScriptDto scriptDto) {
        Object result = null;
        try {
            result = invocable.invokeFunction(scriptDto.getFunctionName(), scriptDto.getParams());
        } catch (javax.script.ScriptException | NoSuchMethodException e) {
            e.printStackTrace();
            throw new ScriptException(e.getMessage());
        }
        return result;
    }
}

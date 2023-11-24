package org.zcx.netty.script;

public interface ScriptRunner {


    public ScriptRunner initScript(ScriptDto scriptDto);

    public Object runFunction(ScriptDto scriptDto);
}

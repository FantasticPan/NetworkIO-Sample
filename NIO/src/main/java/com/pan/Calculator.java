package com.pan;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Created by FantasticPan on 2018/4/18.
 */
public final class Calculator {

    private final static ScriptEngine scriptEngine =
            new ScriptEngineManager().getEngineByName("JavaScript");

    public static Object cal(String expression) throws ScriptException {
        return scriptEngine.eval(expression);
    }
}

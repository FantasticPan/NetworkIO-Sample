import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Created by FantasticPan on 2018/4/18.
 */
public enum  Calculator {

    Instance;

    private final static ScriptEngine scriptEngine =
            new ScriptEngineManager().getEngineByName("JavaScript");

    public Object cal(String expression) throws ScriptException {
        return scriptEngine.eval(expression);
    }
}

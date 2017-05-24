package org.uofm.ot.activator.adapter;

import java.util.Map;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.springframework.stereotype.Component;
import org.uofm.ot.activator.domain.EngineType;
import org.uofm.ot.activator.exception.OTExecutionStackException;

/**
 * Created by nggittle on 5/23/17.
 */
@Component
public class JavaScriptAdapter implements ServiceAdapter {

  @Override
  public Object execute(Map<String, Object> args, String code, String functionName, Class returnType) throws OTExecutionStackException {

    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine = manager.getEngineByName("JavaScript");
    Invocable inv = (Invocable) engine;

    try {

      engine.eval(code);
      return returnType.cast(inv.invokeFunction(functionName, args));

    } catch (ScriptException scriptEx) {
      throw new OTExecutionStackException("Error occurred while executing javascript code SyntaxError: " + scriptEx, scriptEx);
    } catch (NoSuchMethodException noSuchEx) {
      throw new OTExecutionStackException("The function " + functionName + " was not found in the javascript payload.", noSuchEx);
    } catch (ClassCastException classEx) {
      throw new OTExecutionStackException("Type mismatch while converting javascript result to java type " + classEx, classEx);
    } catch (IllegalArgumentException argEx) {
      throw new OTExecutionStackException("Javascript payload is empty or has bad syntax.", argEx);
    } catch (StackOverflowError stackEx) {
      throw new OTExecutionStackException("Stack overflow error. Make sure you don't have infinite recursion or memory leaks.", stackEx);
    }
  }
}

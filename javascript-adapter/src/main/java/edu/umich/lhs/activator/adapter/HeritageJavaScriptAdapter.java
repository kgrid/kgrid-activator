package edu.umich.lhs.activator.adapter;

import edu.umich.lhs.activator.exception.ActivatorException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Created by nggittle on 5/23/17.
 */
public class HeritageJavaScriptAdapter implements EnvironmentAdapter {

  public HeritageJavaScriptAdapter() {}

  @Override
  public Object execute(Map<String, Object> args, String code, String functionName, Class returnType) throws ActivatorException {

    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine = manager.getEngineByName("JavaScript");
    Invocable inv = (Invocable) engine;

    try {

      engine.eval(code);
      return returnType.cast(inv.invokeFunction(functionName, args));

    } catch (ScriptException scriptEx) {
      throw new ActivatorException("Error occurred while executing javascript code SyntaxError: " + scriptEx, scriptEx);
    } catch (NoSuchMethodException noSuchEx) {
      throw new ActivatorException("The function " + functionName + " was not found in the javascript payload.", noSuchEx);
    } catch (ClassCastException classEx) {
      throw new ActivatorException("Type mismatch while converting javascript result to java type " + classEx, classEx);
    } catch (IllegalArgumentException argEx) {
      throw new ActivatorException("Javascript payload is empty or has bad syntax.", argEx);
    } catch (StackOverflowError stackEx) {
      throw new ActivatorException("Stack overflow error. Make sure you don't have infinite recursion or memory leaks.", stackEx);
    }
  }

  @Override
  public List<String> supports(){
    List<String> languages = new ArrayList<>();
    languages.add("JS");
    languages.add("JavaScript");
    return languages;
  }


}

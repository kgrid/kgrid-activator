package edu.umich.lhs.activator.adapter;

import java.util.List;
import java.util.Map;

/**
 * Implement this interface in a different module to create an execution adapter
 *
 * Created by nggittle on 5/19/17.
 */
public interface ServiceAdapter {

  /**
   * A method that executes the specified function in the code string, passing in the map of arguments
   * and returning an object of the specified class;
   * @param args arguments to pass into the called function
   * @param code code to execute
   * @param functionName executed function
   * @param returnType java class for the type of returned object
   * @return the result of running the function
   * @throws Exception if there is an error in the executed code
   */
  Object execute(Map<String, Object> args, String code, String functionName, Class returnType) throws Exception;

  /**
   * The list of languages supported by this adapter. These should match the Engine Type used in the
   * Knowledge Object payload
   * @return a list of supported language names, for most cases this will be only one item long but
   *  can include aliases such as "JavaScript" and "JS" for a JavaScript adapter. The comparison is
   *  not case-sensitive so there is no need to return "JavaScript", "Javascript", and "javascript"
   *  for example.
   */
  List<String> supports();

}

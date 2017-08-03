package edu.umich.lhs.activator.adapter;

import java.util.List;
import java.util.Map;
import edu.umich.lhs.activator.exception.ActivatorException;

/**
 * Created by nggittle on 5/19/17.
 */
public interface ServiceAdapter {

  Object execute(Map<String, Object> args, String code, String functionName, Class returnType) throws Exception;

  List<String> supports();

}

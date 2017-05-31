package org.uofm.ot.activator.adapter;

import java.util.List;
import java.util.Map;
import org.uofm.ot.activator.exception.OTExecutionStackException;

/**
 * Created by nggittle on 5/19/17.
 */
public interface ServiceAdapter {

  Object execute(Map<String, Object> args, String code, String functionName, Class returnType) throws Exception;

  List<String> supports();


}

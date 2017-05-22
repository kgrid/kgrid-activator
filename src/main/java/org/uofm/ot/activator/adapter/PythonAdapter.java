package org.uofm.ot.activator.adapter;

import org.python.core.PyDictionary;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.uofm.ot.activator.exception.OTExecutionStackException;

import java.util.Map;

@Component
public class PythonAdapter implements ServiceAdapter {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public Object execute(Map<String, Object>args, String code, String functionName, Class returnType) {

		PythonInterpreter interpreter = new PythonInterpreter();
		PyDictionary dictionary = new PyDictionary();
		dictionary.putAll(args);

		try {
			interpreter.exec(code);
			PyObject someFunc = interpreter.get(functionName);

			if(someFunc != null) {

				PyObject result = someFunc.__call__(dictionary);

				Object javaResult = result.__tojava__(returnType);
				javaResult = returnType.cast(javaResult);
				return javaResult;

			} else {
				throw new OTExecutionStackException(functionName + " function not found in object payload ");
			}
		} catch (PyException ex) {
			String err = "Error occurred while executing python code " + ex;
			log.error(err);
			throw new OTExecutionStackException(err, ex);
		} catch (ClassCastException exc) {
			String err = "Type mismatch while converting python result to java type " + returnType.getName() + " Check input spec and code return types.";
			log.error(err);
			throw new OTExecutionStackException(err, exc);
		} finally {
			interpreter.close();
		}
	}
}

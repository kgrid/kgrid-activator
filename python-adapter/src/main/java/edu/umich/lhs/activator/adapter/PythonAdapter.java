package edu.umich.lhs.activator.adapter;

import java.util.ArrayList;
import java.util.List;
import org.python.core.PyDictionary;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import edu.umich.lhs.activator.exception.ActivatorException;

import java.util.Map;

public class PythonAdapter implements EnvironmentAdapter {

	public PythonAdapter() {}

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
				throw new ActivatorException(functionName + " function not found in object payload ");
			}
		} catch (PyException ex) {
			String err = "Error occurred while executing python code " + ex;
			throw new ActivatorException(err, ex);
		} catch (ClassCastException exc) {
			String err = "Type mismatch while converting python result to java type " + returnType.getName() + " Check input spec and code return types.";
			throw new ActivatorException(err, exc);
		} finally {
			interpreter.close();
		}
	}

	public List<String> supports() {
		List<String> languages = new ArrayList<>();
		languages.add("Python");
		return languages;
	}
}

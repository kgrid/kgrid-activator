package org.uofm.ot.activator.adapter;

import org.python.core.PyDictionary;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.uofm.ot.activator.domain.KnowledgeObject.Payload;
import org.uofm.ot.activator.domain.Result;
import org.uofm.ot.activator.exception.OTExecutionStackException;

import java.util.Map;

@Component
public class PythonAdapter {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public Result execute(Map<String, Object> params, Payload payload) throws OTExecutionStackException {

		PythonInterpreter interpreter = new PythonInterpreter();

		Result resObj = new Result();

		PyDictionary dictionary = new PyDictionary();
		dictionary.putAll(params); // NPE for null params, ok for empty map
		try {
			interpreter.exec(payload.content); // PyException on unparsable code, NPE on null content

			PyObject someFunc = interpreter.get(payload.functionName); // NPE on null functionName, pyEx on name not found

			PyObject result = someFunc.__call__(dictionary); // pyEx on 'raise ValueError("message")

			resObj.setResult(result.__tojava__(Object.class));

//		} catch(PyException ex) {
//			log.error("Exception occurred while executing python code " + ex.getMessage());
//			throw new OTExecutionStackException("Error while executing payload code " + ex);
		} finally {
			interpreter.close();
		}

		return resObj;
	}

}

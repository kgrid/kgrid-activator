package org.uofm.ot.activator.adapter;

import org.python.core.PyDictionary;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.uofm.ot.activator.exception.OTExecutionStackException;
import org.uofm.ot.activator.transferObjects.DataType;
import org.uofm.ot.activator.transferObjects.KnowledgeObjectDTO.Payload;
import org.uofm.ot.activator.transferObjects.Result;

import java.util.Map;

@Component
public class PythonAdapter {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public Result execute(Map<String, Object> params, Payload payload, DataType returntype) throws OTExecutionStackException {

		PythonInterpreter interpreter = new PythonInterpreter();

		Result resObj;

		PyDictionary dictionary = new PyDictionary();
		dictionary.putAll(params);
		try {
			interpreter.exec(payload.content);

			PyObject someFunc = interpreter.get(payload.functionName);
			if(someFunc != null) {
				PyObject result = someFunc.__call__(dictionary);

				resObj = mapResult(returntype, result);
			} else {
				log.error(payload.functionName + " function not found in object payload ");
				throw new OTExecutionStackException(payload.functionName + " function not found in object payload ");
			}

		} catch(PyException ex) {
			log.error("Exception occurred while executing python code "+ex.getMessage());
			throw new OTExecutionStackException("Error while executing payload code " + ex);
		} finally {
			interpreter.close();
		}

		return resObj;
	}

	private Result mapResult(DataType returntype, PyObject result) {

		Result resObj = new Result();

		try {
			if (DataType.FLOAT == returntype) {
				Float realResult = (Float) result.__tojava__(float.class);

				resObj.setResult(String.valueOf(realResult));
			} else {
				if (DataType.INT == returntype) {
					int realResult = (int) result.__tojava__(int.class);

					resObj.setResult(String.valueOf(realResult));
				} else {
					if (DataType.STRING == returntype) {
						String realResult = (String) result.__tojava__(String.class);
						resObj.setResult(realResult);

					} else {
						if (DataType.MAP == returntype) {
							Map<String, Object> realMap = (Map<String, Object>) result.__tojava__(Map.class);
							resObj.setResult(realMap.get("result"));
						}
					}
				}
			}
		} catch (ClassCastException ex) {
			throw new OTExecutionStackException("Type mismatch while converting python result to java. Check input spec and code payload " + ex);
		}
    return resObj;
	}
}

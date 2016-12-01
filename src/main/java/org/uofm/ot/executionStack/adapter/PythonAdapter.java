package org.uofm.ot.executionStack.adapter;

import java.util.Map;


import org.python.core.PyDictionary;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import org.springframework.stereotype.Component;
import org.uofm.ot.executionStack.exception.OTExecutionStackException;
import org.uofm.ot.executionStack.transferObjects.DataType;
import org.uofm.ot.executionStack.transferObjects.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
public class PythonAdapter {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public Result executeString(String chunk,String functionName,Map<String,Object> params, DataType returntype) throws OTExecutionStackException {


		PythonInterpreter interpreter = new PythonInterpreter();

		Result resObj = new Result();


		PyDictionary dictionary = new PyDictionary();
		dictionary.putAll(params);		
		interpreter.exec(chunk);



		try {
			PyObject someFunc = interpreter.get(functionName);
			if(someFunc != null) {
				PyObject result = someFunc.__call__(dictionary);

				if(DataType.FLOAT == returntype){
					Float realResult = (Float) result.__tojava__(float.class);

					resObj.setErrorMessage("-");
					resObj.setSuccess(1);
					resObj.setResult(String.valueOf(realResult));
				} else {
					if(DataType.INT == returntype){
						int realResult = (int) result.__tojava__(int.class);

						resObj.setErrorMessage("-");
						resObj.setSuccess(1);
						resObj.setResult(String.valueOf(realResult));
					} else {
						if(DataType.STRING == returntype) {
							String realResult = (String) result.__tojava__(String.class);
							resObj.setErrorMessage("-");
							resObj.setSuccess(1);
							resObj.setResult(realResult);

						} else {
							if(DataType.MAP == returntype) {
								Map<String,Object> realMap = (Map<String,Object>) result.__tojava__(Map.class);
								resObj.setErrorMessage((String)realMap.get("errorMessage"));
								resObj.setSuccess((int)realMap.get("success"));
								resObj.setResult(realMap.get("result"));
							}
						}
					}
				}
			} else {
				log.error(functionName + " function not found in object payload ");
				OTExecutionStackException exception = new OTExecutionStackException(functionName + " function not found in object payload ");
				throw exception;
			}

		} catch(PyException ex) {
			log.error("Exception occured while executing python code "+ex.getMessage());
			resObj.setErrorMessage(ex.getMessage());
			resObj.setSuccess(0);
		} finally {
			interpreter.close();
		}
		
		return resObj;
	}
}

package org.uofm.ot.activator.adapter;

import java.util.ArrayList;
import java.util.List;
import org.uofm.ot.activator.adapter.gateway.RestClient;
import org.uofm.ot.activator.exception.OTExecutionStackException;

import java.util.Map;

public class JupyterKernelAdapter implements ServiceAdapter {

  public RestClient restClient;

	public JupyterKernelAdapter() {}

	public Object execute(Map<String, Object>args, String code, String functionName, Class returnType) {
		if (code == "") {
			throw new OTExecutionStackException(functionName + " function not found in object payload ");
		}
		return new Object();		
	}

	//What is the kernel ID of the the jupyter kernel I'll be sending the payload to?
	public String selectKernel(){
		//Get list of existing kernels

		//Ask to create a new kernel if one is not available.

	  return "";
	}

	public List<String> supports() {
		List<String> languages = new ArrayList<>();
		languages.add("Python");
		return languages;
	}
}

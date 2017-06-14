package org.uofm.ot.activator.adapter;

import java.util.ArrayList;
import java.util.List;
import org.uofm.ot.activator.adapter.gateway.RestClient;
import org.uofm.ot.activator.exception.OTExecutionStackException;
import org.uofm.ot.activator.adapter.gateway.KernelMetadata;
import java.net.URI;

import java.util.Map;
import java.util.Collections;

public class JupyterKernelAdapter implements ServiceAdapter {

  public RestClient restClient;

	public JupyterKernelAdapter() {
		this.restClient = new RestClient(URI.create("http://localhost:8888"));
	}

	public Object execute(Map<String, Object>args, String code, String functionName, Class returnType) {
		if (code == "") {
			throw new OTExecutionStackException(functionName + " function not found in object payload ");
		}
		String selectedKernel = selectKernel();
		return new Object();
	}

	//What is the kernel ID of the the jupyter kernel I'll be sending the payload to?
	public String selectKernel(){
		//Get list of existing kernels
		List<KernelMetadata> kernelIDs = restClient.getKernels();

		//Ask to create a new kernel if one is not available.
		if (kernelIDs.size() == 0) {
			throw new OTExecutionStackException(" no available Jupyter Kernels for payload ");
		}
		return "";
	}

	public List<String> supports() {
		List<String> languages = new ArrayList<>();
		languages.add("Python");
		return languages;
	}
}

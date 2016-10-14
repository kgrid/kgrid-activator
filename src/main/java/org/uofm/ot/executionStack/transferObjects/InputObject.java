package org.uofm.ot.executionStack.transferObjects;

import java.util.Map;

public class InputObject {
	
	private String objectName;
	
	public InputObject() {}
	
	private Map<String,Object> params;

	public InputObject(String objectName, Map<String, Object> params) {
		super();
		this.objectName = objectName;
		this.params = params;
	}

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	
	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	@Override
	public String toString() {
		return "InputObject [objectName=" + objectName + ", params=" + params + "]";
	}
	
}

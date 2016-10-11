package org.uofm.ot.executionStack.transferObjects;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;  
import javax.xml.bind.annotation.XmlRootElement;  

public class CodeMetadata {
	

	
	private int noOfParams;
	
	private ArrayList<ParamDescription> params;
	
	private DataType returntype;
	

	public CodeMetadata(){}

	public int getNoOfParams() {
		return noOfParams;
	}

	public void setNoOfParams(int noOfParams) {
		this.noOfParams = noOfParams;
	}

	public ArrayList<ParamDescription> getParams() {
		return params;
	}

	public DataType getReturntype() {
		return returntype;
	}

	public void setReturntype(DataType returntype) {
		this.returntype = returntype;
	}


	public void setParams(ArrayList<ParamDescription> params) {
		this.params = params;
	}	
	
	

}

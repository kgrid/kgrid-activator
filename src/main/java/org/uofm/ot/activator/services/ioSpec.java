package org.uofm.ot.activator.services;

import java.util.ArrayList;
import java.util.Map;
import org.uofm.ot.activator.domain.DataType;
import org.uofm.ot.activator.domain.ParamDescription;


public class ioSpec {
	
	private int noOfParams;
	
	private ArrayList<ParamDescription> params;
	
	private DataType returntype;
	

	public ioSpec(){}

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
	
	public String verifyInput( Map<String,Object> ipParams){
		String errorMessage= null;
		if(this.noOfParams != ipParams.size()){
			errorMessage = "Number of input parameters should be "+this.noOfParams+".";
		}

		for (ParamDescription param : this.params) {
			if(!ipParams.containsKey(param.getName())){
				if(errorMessage == null)
					errorMessage= " Input parameter "+param.getName()+" is missing.";
				else
					errorMessage = errorMessage + " Input parameter "+param.getName()+" is missing.";
			}
		}

		if(errorMessage == null)
			errorMessage = verifyParameters(ipParams);

//		if (errorMessage != null) {
//			throw new OTExecutionStackException("Error in converting RDF metadata for ko: " + errorMessage);
//		}

		return errorMessage;
	}
	
	public String verifyParameters( Map<String,Object> params) {
		String error = null;
		for (ParamDescription paramDescription : this.params) {
			Object obj = params.get(paramDescription.getName());

			if (DataType.INT == paramDescription.getDatatype()){
				try {
					Integer value = Integer.parseInt(obj.toString());
					if(paramDescription.getMin() != null){
						if(value < paramDescription.getMin()) {
							error = " Parameter "+paramDescription.getName()+" should be of minimum value "+paramDescription.getMin();
							break;
						}
					}

					if(paramDescription.getMax() != null){
						if(value > paramDescription.getMax()) {
							error = " Parameter "+paramDescription.getName()+" should be less than maximum allowed value "+paramDescription.getMax();
							break;
						}
					}
					params.replace(paramDescription.getName(), value);
				} catch (NumberFormatException e){
					e.printStackTrace();
					error = " Parameter "+paramDescription.getName()+" should be of type INT";
					break;
				}
			} else {
				if(DataType.FLOAT == paramDescription.getDatatype()){
					try {
						Float value = Float.parseFloat(obj.toString());
						if(paramDescription.getMin() != null){
							if( value < paramDescription.getMin()){
								error = " Parameter "+paramDescription.getName()+" should be of minimum value "+paramDescription.getMin();
								break;
							}
						}

						if(paramDescription.getMax() != null){
							if(value > paramDescription.getMax()){
								error = " Parameter "+paramDescription.getName()+" should be less than maximum allowed value "+paramDescription.getMax();
								break;
							}
						}
					} catch (NumberFormatException e){
						e.printStackTrace();
						error = " Parameter "+paramDescription.getName()+" should be of type FLOAT";
						break;
					}
				} else {
					if(DataType.LONG == paramDescription.getDatatype()){
						try {
							Long value = Long.parseLong(obj.toString());
							if(paramDescription.getMin() != null){
								if( value < paramDescription.getMin()){
									error = " Parameter "+paramDescription.getName()+" should be of minimum value "+paramDescription.getMin();
									break;
								}
							}

							if(paramDescription.getMax() != null){
								if(value > paramDescription.getMax()){
									error = " Parameter "+paramDescription.getName()+" should be less than maximum allowed value "+paramDescription.getMax();
									break;
								}
							}
						} catch (NumberFormatException e){
							e.printStackTrace();
							error = " Parameter "+paramDescription.getName()+" should be of type LONG";
							break;
						}
					} 
				} 
			}
		}
		return error;
	}

}

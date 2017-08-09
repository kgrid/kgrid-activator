package edu.umich.lhs.activator.domain;

public class ParamDescription {
	
	private String name;
	
	private DataType datatype;
	
	private Integer min;
	
	private Integer max;

	public ParamDescription(){}
	
	public ParamDescription(String name, DataType datatype, Integer min, Integer max) {
		super();
		this.name = name;
		this.datatype = datatype;
		this.min = min;
		this.max = max;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DataType getDatatype() {
		return datatype;
	}

	public void setDatatype(DataType datatype) {
		this.datatype = datatype;
	}

	public Integer getMin() {
		return min;
	}

	public void setMin(Integer min) {
		this.min = min;
	}

	public Integer getMax() {
		return max;
	}

	public void setMax(Integer max) {
		this.max = max;
	}

	@Override
	public String toString() {
		return "ParamDescription [name=" + name + ", datatype=" + datatype + ", min=" + min + ", max=" + max + "]";
	}
	
	

}

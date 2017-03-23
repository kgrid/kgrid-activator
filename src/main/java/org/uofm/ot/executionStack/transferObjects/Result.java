package org.uofm.ot.executionStack.transferObjects;

public class Result {
	
	private int success;
	
	private Object result;
	
	private String errorMessage;
	
	private String source;
		public Result(){}

	public Result(int success, String result, String errorMessage) {
		super();
		this.success = success;
		this.result = result;
		this.errorMessage = errorMessage;
	}

	
	public int getSuccess() {
		return success;
	}

	public void setSuccess(int success) {
		this.success = success;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	@Override
	public String toString() {
		return "Result [success=" + success
				+ ", result=" + result
				+ ", errorMessage=" + errorMessage
				+ ", source=" + source
				+ "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Result result1 = (Result) o;

		if (success != result1.success) {
			return false;
		}
		if (result != null ? !result.equals(result1.result) : result1.result != null) {
			return false;
		}
		if (errorMessage != null ? !errorMessage.equals(result1.errorMessage)
				: result1.errorMessage != null) {
			return false;
		}
		return source != null ? source.equals(result1.source) : result1.source == null;
	}

	@Override
	public int hashCode() {
		int result1 = success;
		result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
		result1 = 31 * result1 + (errorMessage != null ? errorMessage.hashCode() : 0);
		result1 = 31 * result1 + (source != null ? source.hashCode() : 0);
		return result1;
	}
}

package edu.umich.lhs.activator.domain;

public class Result {

	private Object result;

	private String source;

	private Metadata metadata;

	public Result(){}

	public Result(String result) {
		super();
		this.result = result;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
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

		if (result != null ? !result.equals(result1.result) : result1.result != null) {
			return false;
		}
		if (source != null ? !source.equals(result1.source) : result1.source != null) {
			return false;
		}
		return metadata != null ? metadata.equals(result1.metadata) : result1.metadata == null;
	}

	@Override
	public int hashCode() {
		int result1 = result != null ? result.hashCode() : 0;
		result1 = 31 * result1 + (source != null ? source.hashCode() : 0);
		result1 = 31 * result1 + (metadata != null ? metadata.hashCode() : 0);
		return result1;
	}

	@Override
	public String toString() {
		return "Result{" +
				"result=" + result +
				", source='" + source + '\'' +
				", metadata=" + metadata +
				'}';
	}

}

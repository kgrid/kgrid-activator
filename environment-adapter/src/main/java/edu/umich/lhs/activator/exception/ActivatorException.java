package edu.umich.lhs.activator.exception;

public class ActivatorException extends RuntimeException {

	public ActivatorException() {
		super();

	}

	public ActivatorException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);

	}

	public ActivatorException(String message, Throwable cause) {
		super(message, cause);

	}

	public ActivatorException(String message) {
		super(message);

	}

	public ActivatorException(Throwable cause) {
		super(cause);

	}
	
	

}

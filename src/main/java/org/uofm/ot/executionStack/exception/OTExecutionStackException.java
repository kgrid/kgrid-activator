package org.uofm.ot.executionStack.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST, reason="Malformed execution inputs") // HTTP 400 Error Bad Request
public class OTExecutionStackException extends RuntimeException {

	public OTExecutionStackException() {
		super();

	}

	public OTExecutionStackException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);

	}

	public OTExecutionStackException(String message, Throwable cause) {
		super(message, cause);

	}

	public OTExecutionStackException(String message) {
		super(message);

	}

	public OTExecutionStackException(Throwable cause) {
		super(cause);

	}
	
	

}

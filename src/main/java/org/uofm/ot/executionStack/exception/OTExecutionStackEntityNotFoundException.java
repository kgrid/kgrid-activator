package org.uofm.ot.executionStack.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.NOT_FOUND) // HTTP 404 Not Found
public class OTExecutionStackEntityNotFoundException  extends OTExecutionStackException {

	public OTExecutionStackEntityNotFoundException() {
		super();

	}

	public OTExecutionStackEntityNotFoundException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);

	}

	public OTExecutionStackEntityNotFoundException(String message, Throwable cause) {

		super(message, cause);

	}

	public OTExecutionStackEntityNotFoundException(String message) {
		super(message);

	}

	public OTExecutionStackEntityNotFoundException(Throwable cause) {
		super(cause);

	}

	
}

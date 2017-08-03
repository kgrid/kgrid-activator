package edu.umich.lhs.activator.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.NOT_FOUND) // HTTP 404 Not Found
public class KONotFoundException extends ActivatorException {

	public KONotFoundException() {
		super();

	}

	public KONotFoundException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);

	}

	public KONotFoundException(String message, Throwable cause) {

		super(message, cause);

	}

	public KONotFoundException(String message) {
		super(message);

	}

	public KONotFoundException(Throwable cause) {
		super(cause);

	}

	
}

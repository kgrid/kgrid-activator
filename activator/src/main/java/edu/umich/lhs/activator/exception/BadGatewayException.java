package edu.umich.lhs.activator.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by nggittle on 3/29/17.
 */

@ResponseStatus(value= HttpStatus.BAD_GATEWAY) // HTTP 502 Bad Gateway
public class BadGatewayException extends ActivatorException {

  public BadGatewayException() {
    super();

  }

  public BadGatewayException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);

  }

  public BadGatewayException(String message, Throwable cause) {

    super(message, cause);

  }

  public BadGatewayException(String message) {
    super(message);

  }

  public BadGatewayException(Throwable cause) {
    super(cause);

  }
}

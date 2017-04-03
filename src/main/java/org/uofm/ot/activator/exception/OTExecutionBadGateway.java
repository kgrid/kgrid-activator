package org.uofm.ot.activator.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by nggittle on 3/29/17.
 */

@ResponseStatus(value= HttpStatus.BAD_GATEWAY) // HTTP 502 Bad Gateway
public class OTExecutionBadGateway extends OTExecutionStackException {

  public OTExecutionBadGateway() {
    super();

  }

  public OTExecutionBadGateway(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);

  }

  public OTExecutionBadGateway(String message, Throwable cause) {

    super(message, cause);

  }

  public OTExecutionBadGateway(String message) {
    super(message);

  }

  public OTExecutionBadGateway(Throwable cause) {
    super(cause);

  }
}

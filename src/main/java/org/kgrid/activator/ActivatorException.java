package org.kgrid.activator;

import org.springframework.http.HttpStatus;

public class ActivatorException extends RuntimeException {

  public ActivatorException(String message){
    super(message);
  }

  public ActivatorException(String message, Throwable cause) {
    super(message, cause);
  }

}

package org.kgrid.activator;

import org.springframework.http.HttpStatus;

public class ActivatorException extends RuntimeException {

  private HttpStatus status;

  public ActivatorException(String message, HttpStatus status){
    super(message);
    this.status = status;
  }

  public ActivatorException(String message, Throwable cause, HttpStatus status) {
    super(message, cause);
    this.status = status;
  }

  public HttpStatus getStatus(){
    return this.status;
  }
}

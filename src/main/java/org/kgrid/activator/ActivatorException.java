package org.kgrid.activator;

import org.springframework.http.HttpStatus;

public class ActivatorException extends RuntimeException {

  private HttpStatus status;

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

  public ActivatorException(String title, String message, Throwable cause) {

  }

  public ActivatorException(String title, String message) {

  }

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

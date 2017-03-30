package org.uofm.ot.executionStack.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.uofm.ot.executionStack.transferObjects.ErrorInfo;

/**
 * Handles errors across all controllers
 * Created by nggittle on 3/29/17.
 */
@ControllerAdvice
public class ResponseExceptionHandler {

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(value = {OTExecutionStackException.class})
  @ResponseBody ErrorInfo
  handleBadRequest(RuntimeException ex, WebRequest request) {
    return new ErrorInfo(ex.getLocalizedMessage(), ex.getMessage(), request.getContextPath());
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(value = {OTExecutionStackEntityNotFoundException.class})
  @ResponseBody ErrorInfo
  handleEntityNotFound(RuntimeException ex, WebRequest request) {
    return new ErrorInfo(ex.getLocalizedMessage(), ex.getMessage(), request.getContextPath());
  }

  @ResponseStatus(HttpStatus.BAD_GATEWAY)
  @ExceptionHandler(value = {OTExecutionBadGateway.class})
  @ResponseBody ErrorInfo
  handleBadGateway(RuntimeException ex, WebRequest request) {
    return new ErrorInfo(ex.getLocalizedMessage(), ex.getMessage(), request.getContextPath());
  }
  //TODO: Add more handlers?

}

package org.kgrid.activator.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.kgrid.activator.ActivatorException;
import org.kgrid.activator.EndPointResult;
import org.kgrid.activator.services.ActivationService;
import org.kgrid.adapter.api.AdapterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

@RestController
@CrossOrigin
@Primary
public class ActivationController {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private ActivationService activationService;

  @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE},
      value = {"/{naan}/{name}/{implementation}/{endpoint}"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @ResponseStatus(HttpStatus.OK)
  public Object processWithBareJsonInputOutput(
      @PathVariable String naan,
      @PathVariable String name,
      @PathVariable String implementation ,
      @PathVariable String endpoint,
      @RequestBody Object inputs) {
    final String key = naan + "-" + name + "/" + implementation + "/" + endpoint;

    try {
      EndPointResult result = activationService.execute(key, inputs);
      return result;
    } catch (AdapterException e) {
      log.error("Exception " + e);
      throw new ActivatorException("Exception for endpoint " + key + " " + e.getMessage());
    }
  }


  @ExceptionHandler(ActivatorException.class)
  public ResponseEntity<Map<String, String>> handleGeneralActivatorExceptions(ActivatorException e,
      WebRequest request) {
    return new ResponseEntity<>(generateErrorMap(request, e, "Activator Error", HttpStatus.BAD_REQUEST),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleGeneralExceptions(Exception e,
      WebRequest request) {
    return new ResponseEntity<>(generateErrorMap(request, e, "Error", HttpStatus.INTERNAL_SERVER_ERROR),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private Map<String, String> generateErrorMap(WebRequest request, Exception e, String title,
      HttpStatus status) {
    Map<String, String> errorInfo = new HashMap<>();
    errorInfo.put("Title", title);
    errorInfo.put("Status", status.value() + " " + status.getReasonPhrase());
    errorInfo.put("Detail", e.getMessage());
    errorInfo.put("Instance", request.getDescription(false));
    errorInfo.put("Time", new Date().toString());
    return errorInfo;

  }


}

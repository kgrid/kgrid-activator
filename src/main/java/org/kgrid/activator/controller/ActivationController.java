package org.kgrid.activator.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.kgrid.activator.ActivatorException;
import org.kgrid.activator.EndPoint;
import org.kgrid.activator.EndPointResult;
import org.kgrid.activator.services.ActivationService;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.resource.ResourceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
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
  private ActivationService service;

  @GetMapping("/endpoints")
  public Set<String> reloadAndActivateEndPoints() {
    log.info("Reload and activiate endpoints");
    service.loadAndActivateEndPoints();
    return service.getEndpoints().keySet();

  }

  @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE},
      value = {"/{naan}/{name}/{version}/{endpoint}"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @ResponseStatus(HttpStatus.OK)
  public Object processWithBareJsonInputOutput(@PathVariable String naan, @PathVariable String name,
      @PathVariable String version, @PathVariable String endpoint,
      @RequestBody Object inputs) {

    final String key = naan + "/" + name + "/" + version + "/" + endpoint;
    log.info("Execute endpoint  " + key);

    if (service.getEndpoints().containsKey(key)){

      try {

        EndPoint endPoint = service.getEndpoints().get(key);

        EndPointResult<Object> result = new EndPointResult<>(endPoint.executeEndPoint(inputs));
        result.getInfo().put("inputs", inputs);
        result.getInfo().put("ko", naan + "/" + name + "/" + version);

        return result;

      } catch (AdapterException e) {
        log.error("Exception " + e);
        throw new ActivatorException("Exception for endpoint " + key + " " + e.getMessage());
      }

    } else {

      log.error("No endpoint found for path " + key);
      throw new ActivatorException("No endpoint found for path " + key);

    }

  }

  @GetMapping(value = {"{naan}/{name}/{version}/{endpoint}"}, produces = {"*/*"})
  @ResponseStatus(HttpStatus.OK)
  public Object getResource(@PathVariable String naan, @PathVariable String name,
      @PathVariable String version, @PathVariable String endpoint) {

    final String key = naan + "/" + name + "/" + version + "/" + endpoint;

    log.info("Get resource at  endpoint  " + key);

    EndPoint endPoint = service.getEndpoints().get(key);

    return endPoint.getExecutor().execute(null);
  }

  @ExceptionHandler(ActivatorException.class)
  public ResponseEntity<Map<String, String>> handleGeneralActivatorExceptions(ActivatorException e,
      WebRequest request) {
    return new ResponseEntity<>(generateErrorMap(request, e.getMessage(), HttpStatus.BAD_REQUEST),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleGeneralExceptions(Exception e,
      WebRequest request) {
    return new ResponseEntity<>(generateErrorMap(request, e.getMessage(), HttpStatus.BAD_REQUEST),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private Map<String, String> generateErrorMap(WebRequest request, String message, HttpStatus status) {
    Map<String, String> errorInfo = new HashMap<>();
    errorInfo.put("Status", status.toString());
    errorInfo.put("Error", message);
    errorInfo.put("Request", request.getDescription(false));
    errorInfo.put("Time", new Date().toString());
    return errorInfo;

  }


}

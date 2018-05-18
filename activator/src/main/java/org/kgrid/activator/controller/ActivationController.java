package org.kgrid.activator.controller;

import java.util.Map;
import java.util.Set;
import org.kgrid.activator.EndPointResult;
import org.kgrid.activator.services.ActivationService;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("")
public class ActivationController {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private ActivationService service;


  @GetMapping
  public Set<String> findAllLoadedKnowledgeObjectEndPoints() {

    return service.getEndpointExecutors().keySet();

  }

  @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE},
      value = {"/{naan}/{name}/{version}/{endpoint}"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @ResponseStatus(HttpStatus.OK)
  public Object processKnowledgeObjectEndPoint(@PathVariable String naan, @PathVariable String name,
      @PathVariable String version, @PathVariable String endpoint, @RequestBody Map<String, Object> inputs) {

    final String key = naan + "/" + name + "/" + version + "/" + endpoint;

    Executor executor = service.getEndpointExecutors().get(key);

    EndPointResult result = null;

    try{

      result = new EndPointResult( executor.execute(inputs) );
      result.getInfo().put( "inputs", inputs);
      result.getInfo().put( "ko",  naan + "/" + name + "/" + version);

      return result;

    } catch ( AdapterException e){
      log.error("Exception " + e);
    }

    return result;
  }
}

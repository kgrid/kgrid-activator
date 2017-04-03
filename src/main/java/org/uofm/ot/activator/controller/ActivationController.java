package org.uofm.ot.activator.controller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.uofm.ot.activator.services.ActivationService;
import org.uofm.ot.activator.domain.ArkId;
import org.uofm.ot.activator.domain.Result;

/**
 *
 * Created by nggittle on 3/22/2017.
 */

@RestController
@CrossOrigin
public class ActivationController {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private ActivationService service;

  @PostMapping(value = "/knowledgeObject/ark:/{naan}/{name}/result",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @ResponseStatus(HttpStatus.OK)
  public Result getResultByArkId(@RequestBody Map<String, Object> inputs, ArkId arkId) {

    log.info("Sending arkId " + arkId + " to service." );
    Result result = service.getResultByArkId(inputs, arkId);

    return result;
  }

}

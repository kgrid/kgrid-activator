package org.uofm.ot.activator.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.uofm.ot.activator.adapter.ServiceAdapter;
import org.uofm.ot.activator.repository.RemoteShelf;
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

  @Autowired
  private RemoteShelf remoteShelf;

  @PostMapping(value = "/knowledgeObject/ark:/{naan}/{name}/result",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @ResponseStatus(HttpStatus.OK)
  public Result getResultByArkId(@RequestBody Map<String, Object> inputs, ArkId arkId) {

    log.info("Sending arkId " + arkId + " to service." );
    Result result = service.getResultByArkId(inputs, arkId);
    result.setSource(remoteShelf.getRemoteObjectURL(arkId));
    return result;
  }

  @GetMapping(value = "/adapters")
  public Map<String, Set<String>> listAdapters() {
    Map<String, Class> adapters = service.getAdapterList();
    Map<String, Set<String>> loadedAdapters = new HashMap<>();
    loadedAdapters.put("Adapters", adapters.keySet());
    return loadedAdapters;
  }

}

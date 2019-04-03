package org.kgrid.activator.endpoint;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.kgrid.activator.EndpointLoader;
import org.kgrid.activator.services.ActivationService;
import org.kgrid.shelf.domain.ArkId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.stereotype.Component;

/**
 * Activate Endpoint allows re-activation if the entire shell or a particular knowledge object.
 *
 */
@Component
@Endpoint(id = "activate")
public class ActivateEndpoint {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private ActivationService activationService;

  @Autowired
  private EndpointLoader endpointLoader;

  @Autowired
  private Map<String, org.kgrid.activator.services.Endpoint> endpoints;

  /**
   * Re Activates all of the endpoints
   *
   * @return set of activated endpoint paths
   */
  @ReadOperation
  public Set<String> reActivate() {

    log.info("ReActivate");

    endpoints.clear();
    endpoints.putAll(endpointLoader.load());
    activationService.activate(endpoints);

    return endpoints.keySet();
  }

  /**
   * Reactivates all the endpoints for a KO
   *
   * @param naan ko naan
   * @param name ko name
   * @return set of activated endpoint paths
   */
  @ReadOperation
  public Set<String> reActivateKO(@Selector String naan,
      @Selector String name) {

    log.info("ReActivate naan:{} name:{}", naan, name );

    endpoints.entrySet().removeIf(
        e -> e.getKey().startsWith(naan+"-"+name));

    Map<String, org.kgrid.activator.services.Endpoint>
        loadedEndpoints = endpointLoader.load(new ArkId(naan,name));

    activationService.activate(loadedEndpoints);

    endpoints.putAll(loadedEndpoints);

    return endpoints.keySet();
  }


}

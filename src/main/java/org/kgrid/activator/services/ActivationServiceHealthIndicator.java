package org.kgrid.activator.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class ActivationServiceHealthIndicator implements HealthIndicator {

  @Autowired
  private ActivationService activationService;
  @Autowired
  private AdapterService adapterService;


  @Override
  public Health health() {

    if (adapterService.getLoadedAdapters().size() > 0) {
      return Health.up()
          .withDetail("Knowledge Objects found", activationService.getKnowledgeObjectsFound())
          .withDetail("Adapters loaded", adapterService.getLoadedAdapters().keySet())
          .withDetail("EndPoints loaded", activationService.getEndpoints().keySet())
          .build();
    } else {
      return Health.down()
          .withDetail("there are now adapters loaded", adapterService.getLoadedAdapters())
          .build();
    }

  }
}

package org.kgrid.activator.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class ActivationServiceHealthIndicator implements HealthIndicator {

  @Autowired
  private ActivationService activationService;


  @Override
  public Health health() {

    if (activationService.getLoadedAdapters().size() > 0) {
      return Health.up()
          .withDetail("Knowledge Objects found", activationService.getKnowledgeObjectsFound())
          .withDetail("Adapters loaded", activationService.getLoadedAdapters().keySet())
          .withDetail("EndPoints loaded", activationService.getEndpoints().keySet())
          .build();
    } else {
      return Health.down()
          .withDetail("there are now adapters loaded", activationService.getLoadedAdapters())
          .build();
    }

  }
}

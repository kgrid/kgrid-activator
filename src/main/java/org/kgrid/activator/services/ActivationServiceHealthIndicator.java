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
  private AdapterResolver adapterResolver;


  @Override
  public Health health() {

    if (adapterResolver.getAdapters().size() > 0) {
      return Health.up()
          .withDetail("Adapters loaded", adapterResolver.getAdapters().keySet())
          .withDetail("Endpoints loaded", activationService.getEndpoints().keySet())
          .build();
    } else {
      return Health.down()
          .withDetail("there are now adapters loaded", adapterResolver
              .getAdapters())
          .build();
    }

  }
}

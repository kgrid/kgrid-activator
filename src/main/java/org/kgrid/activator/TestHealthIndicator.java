package org.kgrid.activator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class TestHealthIndicator implements HealthIndicator {


  @Override
  public Health health() {
    return Health.outOfService().build();
  }
}

package org.kgrid.activator.services;

import java.util.Map;
import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class ActivationServiceHealthIndicator implements HealthIndicator {

  @Autowired private Map<EndpointId, Endpoint> endpoints;
  @Autowired private KnowledgeObjectRepository repository;


  @Override
  public Health health() {

    final int kos = repository.findAll().size();
    final int eps = endpoints.size();
    boolean somethingWorked = kos == 0 || (eps >= kos);

    Builder status = somethingWorked ? Health.up() : Health.down();

    return status
        .withDetail("kos", kos)
        .withDetail("endpoints", eps)
        .build();
  }
}

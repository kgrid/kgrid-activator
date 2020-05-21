package org.kgrid.activator.services;

import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ActivationServiceHealthIndicator implements HealthIndicator {

  @Autowired private Map<EndpointId, Endpoint> endpoints;
  @Autowired private KnowledgeObjectRepository repository;

  @Override
  public Health health() {

    int kos = repository.findAll().size();
    int eps = endpoints.size();
    boolean hasNoObjectsOrAtLeastOneEndpoint = (kos == 0 || eps > 0);

    Builder status = hasNoObjectsOrAtLeastOneEndpoint ? Health.up() : Health.down();

    return status.withDetail("kos", kos).withDetail("endpoints", eps).build();
  }
}

package org.kgrid.activator.services;

import java.util.Collection;
import java.util.HashMap;
import org.kgrid.activator.constants.EndpointStatus;
import org.kgrid.activator.domain.Endpoint;
import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ActivationServiceHealthIndicator implements HealthIndicator {

    private final ActivationService activationService;
    private final KnowledgeObjectRepository repository;

    public ActivationServiceHealthIndicator(ActivationService activationService, KnowledgeObjectRepository repository) {
        this.activationService = activationService;
        this.repository = repository;
    }

    @Override
    public Health health() {

        Collection<Endpoint> endpoints = activationService.getEndpoints();

        Map<String, Integer> eps = new HashMap<>();
        int kos = repository.findAll().size();
        endpoints.forEach((endpoint) -> {
            eps.merge(endpoint.getStatus(), 1, Integer::sum);
            eps.merge("total", 1, Integer::sum);
        });
        boolean hasNoObjectsOrAtLeastOneEndpoint = (kos == 0 || eps.size() > 0);

        Builder status = hasNoObjectsOrAtLeastOneEndpoint ? Health.up() : Health.down();

        return status
            .withDetail("kos", kos)
            .withDetail("endpoints", eps)
            .build();
    }
}

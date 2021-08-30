package org.kgrid.activator.services;

import java.util.*;

import org.kgrid.activator.domain.Endpoint;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class ActivationServiceHealthIndicator implements HealthIndicator {

    private final ActivationService activationService;

    public ActivationServiceHealthIndicator(ActivationService activationService) {
        this.activationService = activationService;
    }

    @Override
    public Health health() {

        Collection<Endpoint> endpoints = activationService.getEndpoints();
        HashSet<ArkId> arks = new HashSet<>();
        Map<String, Integer> eps = new HashMap<>();
        endpoints.forEach((endpoint) -> {
            arks.add(endpoint.getArkId());
            eps.merge(endpoint.getStatus(), 1, Integer::sum);
            eps.merge("total", 1, Integer::sum);
        });
        boolean hasNoObjectsOrAtLeastOneEndpoint = (arks.size() == 0 || eps.size() > 0);

        Builder status = hasNoObjectsOrAtLeastOneEndpoint ? Health.up() : Health.down();

        return status
                .withDetail("kos", arks.size())
                .withDetail("endpoints", eps)
                .build();
    }
}

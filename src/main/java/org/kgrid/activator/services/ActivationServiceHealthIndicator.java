package org.kgrid.activator.services;

import org.kgrid.activator.constants.EndpointStatus;
import org.kgrid.activator.domain.Endpoint;
import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ActivationServiceHealthIndicator implements HealthIndicator {

    @Autowired
    private Map<URI, Endpoint> endpoints;
    @Autowired
    private KnowledgeObjectRepository repository;

    @Override
    public Health health() {

        int kos = repository.findAll().size();
        int eps = endpoints.size();
        AtomicInteger activatedEps = new AtomicInteger();
        endpoints.forEach((endpointId, endpoint) -> {
            if (endpoint.getStatus().equals(EndpointStatus.LOADED.name())
                    || endpoint.getStatus().equals(EndpointStatus.ACTIVATED.name())) {
                activatedEps.getAndIncrement();
            }
        });
        boolean hasNoObjectsOrAtLeastOneEndpoint = (kos == 0 || eps > 0);

        Builder status = hasNoObjectsOrAtLeastOneEndpoint ? Health.up() : Health.down();

        return status.withDetail("kos", kos).withDetail("endpoints", eps).withDetail("activatedEndpoints", activatedEps).build();
    }
}

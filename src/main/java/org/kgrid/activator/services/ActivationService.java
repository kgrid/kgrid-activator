package org.kgrid.activator.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.kgrid.activator.constants.EndpointStatus;
import org.kgrid.activator.exceptions.ActivatorException;
import org.kgrid.activator.domain.Endpoint;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class ActivationService {

    final Logger log = LoggerFactory.getLogger(this.getClass());

    private Map<URI, Endpoint> endpoints;

    private AdapterResolver adapterResolver;

    private final KnowledgeObjectRepository koRepo;

    public ActivationService(AdapterResolver adapterResolver, Map<URI, Endpoint> endpoints, KnowledgeObjectRepository koRepo) {
        this.adapterResolver = adapterResolver;
        this.endpoints = endpoints;
        this.koRepo = koRepo;
    }

    public void activateEndpoints(Map<URI, Endpoint> eps) {
        eps.forEach((key, value) -> {

            synchronized (value) {
                Executor executor = null;
                try {
                    executor = activateEndpoint(key, value);
                    value.setActivated(LocalDateTime.now());
                    value.setStatus(EndpointStatus.ACTIVATED.name());
                    value.setDetail(null);
                } catch (Exception e) {
                    String message = "Could not activate " + key + ". Cause: " + e.getMessage();
                    log.warn(message + ". " + e.getClass().getSimpleName());
                    value.setActivated(LocalDateTime.now());
                    value.setStatus(EndpointStatus.FAILED_TO_ACTIVATE.name());
                    value.setDetail(message);
                }
                value.setExecutor(executor);
            }

        });
    }

    private Executor activateEndpoint(URI endpointKey, Endpoint endpoint) {
        if(endpoint.isActive()) {
            log.info("Reactivating endpoint: {}", endpointKey);
        } else {
            log.info("Activating endpoint: {}", endpointKey);
        }

        final JsonNode deploymentSpec = endpoint.getDeployment();
        Adapter adapter = adapterResolver.getAdapter(endpoint.getEngine());

        Executor executor =  adapter.activate(
                    koRepo.getObjectLocation(endpoint.getArkId()),
                    endpointKey,
                    deploymentSpec);

        return executor;
    }
}

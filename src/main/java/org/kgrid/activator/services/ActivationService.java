package org.kgrid.activator.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.kgrid.activator.ActivatorException;
import org.kgrid.activator.EndPointResult;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
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

    public void activate(Map<URI, Endpoint> eps) {
        eps.forEach((key, value) -> {
            if (eps.get(key).getStatus().equals("GOOD")) {
                Executor executor = null;
                try {
                    executor = getExecutor(key, value);
                    value.setStatus("Activated");
                } catch (ActivatorException e) {
                    log.warn("Could not activate " + key + " " + e.getMessage());
                    value.setStatus("Could not be activated: " + e.getMessage());
                }
                value.setExecutor(executor);
            }
        });
    }

    private Executor getExecutor(URI endpointKey, Endpoint endpoint) {

        log.info("Activate endpoint {} ", endpointKey);
        final JsonNode deploymentSpec = endpoint.getDeployment();

        if (null == deploymentSpec) {
            throw new ActivatorException("No deployment specification for " + endpointKey);
        }
        String engineName;
        if (deploymentSpec.has("engine")) {
            engineName = deploymentSpec.get("engine").asText();
        } else {
            throw new ActivatorException("No engine specified for " + endpointKey);
        }

        Adapter adapter = adapterResolver
                .getAdapter(engineName);
        ArkId ark = endpoint.getArkId();

        try {
            return adapter.activate(
                    koRepo.getObjectLocation(ark),
                    endpointKey,
                    deploymentSpec);
        } catch (RuntimeException e) {
            endpoints.get(endpointKey).setStatus("Adapter could not create executor: " + e.getMessage());
            throw new ActivatorException(e.getMessage(), e);
        }

    }

    public EndPointResult execute(URI id, Object inputs, String contentType) {
        Endpoint endpoint = endpoints.get(id);

        if (null == endpoint) {
            throw new ActivatorException("No endpoint found for " + id);
        }
        Executor executor = endpoint.getExecutor();

        if (null == executor) {
            throw new ActivatorException("No executor found for " + id);
        }

        final Object output = executor.execute(inputs, contentType);

        final EndPointResult endPointResult = new EndPointResult(output);

        endPointResult.getInfo().put("inputs", inputs);
        endPointResult.getInfo().put("ko", endpoint.getMetadata());

        return endPointResult;
    }

}



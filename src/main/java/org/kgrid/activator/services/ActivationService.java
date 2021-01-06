package org.kgrid.activator.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.kgrid.activator.exceptions.ActivatorEndpointNotFoundException;
import org.kgrid.activator.exceptions.ActivatorException;
import org.kgrid.activator.EndPointResult;
import org.kgrid.activator.exceptions.ActivatorUnsupportedMediaTypeException;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.ShelfResourceNotFound;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

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
            if (value.getStatus().equals("GOOD")) {
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
        } catch (AdapterException e) {
            endpoints.get(endpointKey).setStatus("Adapter could not create executor: " + e.getMessage());
            throw new ActivatorException(e.getMessage(), e);
        } catch (ShelfResourceNotFound e) {
            endpoints.get(endpointKey).setStatus(String.format(
                    "Adapter could not find endpoint: %s while creating executor: %s", endpointKey, e.getMessage()));
            throw new ActivatorException(e.getMessage(), e);
        }

    }

    public EndPointResult execute(URI id, Object inputs, HttpMethod method, String contentType) {
        Endpoint endpoint = endpoints.get(id);

        if (null == endpoint || !endpoint.isActive()) {
            throw new ActivatorEndpointNotFoundException("No active endpoint found for " + id);
        }

        validateContentType(method, contentType, endpoint);

        Executor executor = endpoint.getExecutor();
        Object output = executor.execute(inputs, contentType);

        final EndPointResult endPointResult = new EndPointResult(output);

        endPointResult.getInfo().put("inputs", inputs);
        endPointResult.getInfo().put("ko", endpoint.getMetadata());

        return endPointResult;
    }

    private void validateContentType(HttpMethod method, String contentType, Endpoint endpoint) {
        if (method == HttpMethod.POST) {
            final JsonNode contentTypes = endpoint.getService().at("/paths").get("/" + endpoint.getEndpointName())
                    .get("post").get("requestBody").get("content");
            AtomicBoolean matches = new AtomicBoolean(false);

            contentTypes.fieldNames().forEachRemaining(key -> {
                if (contentType.equals(key)) {
                    matches.set(true);
                }
            });
            if (!matches.get()) {
                ArrayList<String> supportedTypes = new ArrayList<>();
                contentTypes.fieldNames().forEachRemaining(key -> {
                    supportedTypes.add(key);
                });
                throw new ActivatorUnsupportedMediaTypeException(
                        String.format("Endpoint %s does not support media type %s. Supported Content Types: %s",
                                endpoint.getId(), contentType, supportedTypes));
            }
        }
    }

}



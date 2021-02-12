package org.kgrid.activator.services;

import org.kgrid.activator.constants.EndpointStatus;
import org.kgrid.activator.domain.Endpoint;
import org.kgrid.activator.exceptions.ActivatorException;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class ActivationService {

    final Logger log = LoggerFactory.getLogger(ActivationService.class);

    private List<Adapter> adapters;

    private final Map<URI, Endpoint> endpointMap = new TreeMap<>();

    public Map<URI, Endpoint> getEndpointMap() {
        return endpointMap;
    }

    public void reactivateAll() {
        activateEndpoints(endpointMap);
    }

    public void activateEndpoints(Map<URI, Endpoint> eps) {
        eps.values().forEach(this::activateEndpoint);
        endpointMap.putAll(eps);
    }

    public synchronized void activateEndpoint(Endpoint endpoint) {
        URI endpointKey = endpoint.getId();
        if (endpoint.isActive()) {
            log.info("Reactivating endpoint: {}", endpointKey);
        } else {
            log.info("Activating endpoint: {}", endpointKey);
        }
        try {
            Adapter adapter;
            List<Adapter> matchingAdapters = adapters.stream()
                    .filter(possibleAdapter -> (possibleAdapter.getEngines().contains(endpoint.getEngine())))
                    .collect(Collectors.toList());
            if (matchingAdapters.isEmpty()) {
                throw new ActivatorException("No adapter loaded for engine " + endpoint.getEngine());
            } else {
                adapter = matchingAdapters.get(0);
            }
            Executor executor = adapter.activate(
                    endpoint.getPhysicalLocation(),
                    endpointKey,
                    endpoint.getDeployment());
            setEndpointDetails(endpoint, EndpointStatus.ACTIVATED, null, executor);
        } catch (Exception e) {
            String message = "Could not activate " + endpointKey + ". Cause: " + e.getMessage();
            log.warn(message + ". " + e.getClass().getSimpleName());
            setEndpointDetails(endpoint, EndpointStatus.FAILED_TO_ACTIVATE, message, null);
        }
    }

    private void setEndpointDetails(Endpoint endpoint, EndpointStatus activated, String detail, Executor executor) {
        endpoint.setActivated(LocalDateTime.now());
        endpoint.setStatus(activated.name());
        endpoint.setDetail(detail);
        endpoint.setExecutor(executor);
    }

    public void reactivateEndpoint(URI endpointUri) {
        Map<URI, Endpoint> singleReactivate = new TreeMap<>();
        singleReactivate.put(endpointUri, endpointMap.get(endpointUri));
        activateEndpoints(singleReactivate);
    }

    public void reactivateEngine(String engineName) {
        endpointMap.forEach((uri, endpoint) -> {
            if (endpoint.getEngine().equals(engineName)) {
                reactivateEndpoint(uri);
            }
        });
    }

    public void setAdapters(List<Adapter> adapters) {
        this.adapters = adapters;
    }
}

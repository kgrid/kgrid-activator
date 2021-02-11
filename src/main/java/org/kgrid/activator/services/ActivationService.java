package org.kgrid.activator.services;

import org.kgrid.activator.constants.EndpointStatus;
import org.kgrid.activator.domain.Endpoint;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class ActivationService {

    final Logger log = LoggerFactory.getLogger(ActivationService.class);

    private final AdapterResolver adapterResolver;

    private Map<URI, Endpoint> endpointMap;

    public ActivationService(AdapterResolver adapterResolver) {
        this.adapterResolver = adapterResolver;
    }

    public Map<URI, Endpoint> getEndpointMap() {
        return endpointMap;
    }

    public void setEndpointMap(Map<URI, Endpoint> endpointMap) {
        this.endpointMap = endpointMap;
    }

    public void activateEndpoints(Map<URI, Endpoint> eps) {
        eps.values().forEach(this::activateEndpoint);
    }

    public synchronized void activateEndpoint(Endpoint endpoint) {
        URI endpointKey = endpoint.getId();
        if (endpoint.isActive()) {
            log.info("Reactivating endpoint: {}", endpointKey);
        } else {
            log.info("Activating endpoint: {}", endpointKey);
        }
        try {
            Adapter adapter = adapterResolver.getAdapter(endpoint.getEngine());
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
}

package org.kgrid.activator.services;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.kgrid.activator.constants.EndpointStatus;
import org.kgrid.activator.domain.Endpoint;
import org.kgrid.activator.exceptions.ActivatorEndpointNotFoundException;
import org.kgrid.activator.exceptions.ActivatorException;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.domain.ArkId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ActivationService {

    final Logger log = LoggerFactory.getLogger(ActivationService.class);

    private List<Adapter> adapters;

    private final Map<URI, Endpoint> endpointMap = new TreeMap<>();

    public void activateAll() {
        endpointMap.values().forEach(this::activateEndpoint);
    }

    public void activateForEngine(String engineName) {
        endpointMap.values().stream()
            .filter(endpoint -> endpoint.getEngine().equals(engineName))
            .forEach(this::activateEndpoint);
    }

    public void activateEndpointsAndUpdate(Map<URI, Endpoint> eps) {
        eps.values().forEach(this::activateEndpoint);
        endpointMap.putAll(eps);
    }

    public synchronized void activateEndpoint(Endpoint endpoint) {
        log.info("{} endpoint: {}", endpoint.isActive() ? "Reactivating" : "Activating",
            endpoint.getId());
        Executor executor = null;
        try {
            Adapter adapter = getAdapter(endpoint);
            executor = adapter.activate(
                endpoint.getPhysicalLocation(),
                endpoint.getId(),
                endpoint.getDeployment());
            endpoint.setStatus(EndpointStatus.ACTIVATED.name());
        } catch (Exception e) {
            endpoint.setStatus(EndpointStatus.FAILED_TO_ACTIVATE.name());
            String message =
                "Could not activate " + endpoint.getId() + ". Cause: " + e.getMessage();
            log.warn(message + ". " + e.getClass().getSimpleName());
            endpoint.setDetail(message);
        } finally {
            endpoint.setActivated(LocalDateTime.now());
            endpoint.setExecutor(executor);
        }
    }

    private Adapter getAdapter(Endpoint endpoint) {
        Adapter adapter;
        List<Adapter> matchingAdapters = adapters.stream()
            .filter(possibleAdapter -> (possibleAdapter.getEngines().contains(endpoint.getEngine())))
            .collect(Collectors.toList());
        if (matchingAdapters.isEmpty()) {
            throw new ActivatorException("No adapter loaded for engine " + endpoint.getEngine());
        } else {
            adapter = matchingAdapters.get(0);
        }
        return adapter;
    }

    public void setAdapters(List<Adapter> adapters) {
        this.adapters = adapters;
    }

    public List<Endpoint> getAllVersions(String naan, String name, String endpointName) {
        List<Endpoint> versions = endpointMap.values().stream()
            .filter(e -> e.equalsIgnoreVersion(naan, name, endpointName))
            .collect(Collectors.toList());
        if (versions.isEmpty()) {
            throw new ActivatorEndpointNotFoundException(String.format("No active endpoints found for %s/%s/%s",
                    naan, name, endpointName));
        }
        return versions;
    }

    public Endpoint getDefaultEndpoint(String naan, String name, String apiVersion, String endpoint) {
        if (apiVersion == null) {
            return getAllVersions(naan, name, endpoint).get(0);
        }
        return getEndpoint(URI.create(String.format("%s/%s/%s/%s", naan, name, apiVersion, endpoint)));
    }

    public void putAll(Map<URI, Endpoint> eps) {
        endpointMap.putAll(eps);
    }

    public void clear() {
        endpointMap.clear();
    }

    public Endpoint getEndpoint(URI id) {
        final Endpoint endpoint = endpointMap.get(id);
        if (endpoint == null) {
            throw new ActivatorEndpointNotFoundException(id);
        }
        return endpoint;
    }

    public Collection<Endpoint> getEndpoints() {
        return endpointMap.values();
    }

    public Collection<Endpoint> getEndpointsForArkId(ArkId arkId) {

        return endpointMap.values()
                .stream()
                .filter(endpoint -> arkId.equals(endpoint.getArkId()))
                .collect(Collectors.toList());
    }

    public void remove(Endpoint endpoint) {
        endpointMap.remove(endpoint.getId());
    }
}

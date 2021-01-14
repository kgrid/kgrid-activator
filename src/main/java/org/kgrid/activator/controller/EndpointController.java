package org.kgrid.activator.controller;

import org.kgrid.activator.Utilities.EndpointHelper;
import org.kgrid.activator.exceptions.ActivatorException;
import org.kgrid.activator.services.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class EndpointController extends ActivatorExceptionHandler {

    @Autowired
    private Map<URI, Endpoint> endpoints;

    @Autowired
    private EndpointHelper endpointHelper;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value(("${kgrid.shelf.endpoint:kos}"))
    String shelfRoot;

    @GetMapping(value = "/endpoints", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EndpointResource> findAllEndpoints() {
        log.info("find all endpoints");
        List<EndpointResource> resources = new ArrayList<>();

        for (Map.Entry<URI, Endpoint> entry : endpoints.entrySet()) {
            EndpointResource resource = new EndpointResource(entry.getValue(), shelfRoot);
            resources.add(resource);
        }
        return resources;
    }

    @GetMapping(value = "/endpoints/{engine}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EndpointResource> findEndpointsForEngine(@PathVariable String engine) {
        log.info("find all endpoints for engine " + engine);
        List<EndpointResource> resources = new ArrayList<>();
        for (Map.Entry<URI, Endpoint> entry : endpoints.entrySet()) {
            EndpointResource resource = new EndpointResource(entry.getValue(), shelfRoot);
            if (engine.equals(resource.getEngine())) {
                resources.add(resource);
            }
        }
        return resources;
    }

    @GetMapping(value = "/endpoints/{naan}/{name}/{version}/{endpointName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public EndpointResource findEndpointOldVersion(
            @PathVariable String naan,
            @PathVariable String name,
            @PathVariable String version,
            @PathVariable String endpointName) {
        log.info("getting ko endpoint " + naan + "/" + name);
        URI id = getEndpointId(naan, name, version, endpointName);
        Endpoint endpoint = endpoints.get(id);
        if (endpoint == null) {
            throw new ActivatorException("Cannot find endpoint with id " + id);
        }
        return new EndpointResource(endpoint, shelfRoot);
    }

    @GetMapping(value = "/endpoints/{naan}/{name}/{endpointName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public EndpointResource findEndpoint(
            @PathVariable String naan,
            @PathVariable String name,
            @PathVariable String endpointName,
            @RequestParam(name = "v", required = false) String version) {
        log.info("getting ko endpoint " + naan + "/" + name);
        if (version == null) {
            version = endpointHelper.getDefaultVersion(naan, name, endpointName);
        }
        URI id = getEndpointId(naan, name, version, endpointName);
        Endpoint endpoint = endpoints.get(id);
        if (endpoint == null) {
            throw new ActivatorException("Cannot find endpoint with id " + id);
        }
        return new EndpointResource(endpoint, shelfRoot);
    }

    private URI getEndpointId(String naan, String name, String apiVersion, String endpoint) {
        return URI.create(String.format("%s/%s/%s/%s", naan, name, apiVersion, endpoint));
    }
}

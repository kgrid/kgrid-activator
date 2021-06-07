package org.kgrid.activator.controller;

import org.kgrid.activator.domain.Endpoint;
import org.kgrid.activator.exceptions.ActivatorEndpointNotFoundException;
import org.kgrid.activator.services.ActivationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/endpoints")
public class EndpointController extends ActivatorExceptionHandler {

    @Autowired
    private ActivationService activationService;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("kos")
    String shelfRoot;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EndpointResource> findAllEndpoints() {
        log.info("find all endpoints");
        List<EndpointResource> resources = new ArrayList<>();

        for (Endpoint endpoint : activationService.getEndpoints()) {
            EndpointResource resource = new EndpointResource(endpoint, shelfRoot);
            resources.add(resource);
        }
        return resources;
    }

    @GetMapping(value = "/{engine}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EndpointResource> findEndpointsForEngine(@PathVariable String engine) {
        log.info("find all endpoints for engine " + engine);
        List<EndpointResource> resources = new ArrayList<>();
        for (Endpoint endpoint : activationService.getEndpoints()) {
            if (engine.equals(endpoint.getEngine())) {
                resources.add(new EndpointResource(endpoint, shelfRoot));
            }
        }
        return resources;
    }

    @GetMapping(value = "/{naan}/{name}/{apiVersion}/{endpointName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public EndpointResource findEndpointPathVersion(
            @PathVariable String naan,
            @PathVariable String name,
            @PathVariable String apiVersion,
            @PathVariable String endpointName) {
        log.info("getting ko endpoint " + naan + "/" + name);
        URI id = activationService.createEndpointId(naan, name, apiVersion, endpointName);
        Endpoint endpoint = activationService.getEndpoint(id);
        if (endpoint == null) {
            throw new ActivatorEndpointNotFoundException("Cannot find endpoint with id " + id);
        }
        return new EndpointResource(endpoint, shelfRoot);
    }

    @GetMapping(value = "/{naan}/{name}/{endpointName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EndpointResource> findEndpointQueryVersion(
            @PathVariable String naan,
            @PathVariable String name,
            @PathVariable String endpointName,
            @RequestParam(name = "v", required = false) String apiVersion) {
        log.info("getting ko endpoint " + naan + "/" + name);
        List<EndpointResource> resources = new ArrayList<>();
        if (apiVersion == null) {
            List<Endpoint> endpoints = activationService.getAllVersions(naan, name, endpointName);
            for (Endpoint endpoint : endpoints) {
                resources.add(new EndpointResource(endpoint, shelfRoot));
            }
        } else {
            URI id = activationService.createEndpointId(naan, name, apiVersion, endpointName);
            Endpoint endpoint = activationService.getEndpoint(id);
            if (endpoint == null) {
                throw new ActivatorEndpointNotFoundException("Cannot find endpoint with id " + id);
            }
            resources.add(new EndpointResource(endpoint, shelfRoot));
        }
        return resources;
    }

}

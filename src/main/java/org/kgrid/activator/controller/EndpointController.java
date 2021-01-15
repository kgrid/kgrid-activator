package org.kgrid.activator.controller;

import org.kgrid.activator.Utilities.EndpointHelper;
import org.kgrid.activator.exceptions.ActivatorException;
import org.kgrid.activator.services.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/endpoints")
public class EndpointController extends ActivatorExceptionHandler {

    @Autowired
    private Map<URI, Endpoint> endpoints;

    @Autowired
    private EndpointHelper endpointHelper;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value(("${kgrid.shelf.endpoint:kos}"))
    String shelfRoot;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EndpointResource> findAllEndpoints() {
        log.info("find all endpoints");
        List<EndpointResource> resources = new ArrayList<>();

        for (Map.Entry<URI, Endpoint> entry : endpoints.entrySet()) {
            EndpointResource resource = new EndpointResource(entry.getValue(), shelfRoot);
            resources.add(resource);
        }
        return resources;
    }

    @GetMapping(value = "/{engine}", produces = MediaType.APPLICATION_JSON_VALUE)
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

    @GetMapping(value = "/{naan}/{name}/{apiVersion}/{endpointName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public EndpointResource findEndpointOldVersion(
            @PathVariable String naan,
            @PathVariable String name,
            @PathVariable String apiVersion,
            @PathVariable String endpointName) {
        log.info("getting ko endpoint " + naan + "/" + name);
        URI id = endpointHelper.createEndpointId(naan, name, apiVersion, endpointName);
        Endpoint endpoint = endpoints.get(id);
        if (endpoint == null) {
            throw new ActivatorException("Cannot find endpoint with id " + id);
        }
        return new EndpointResource(endpoint, shelfRoot);
    }

    @GetMapping(value = "/{naan}/{name}/{endpointName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EndpointResource> findEndpoint(
            @PathVariable String naan,
            @PathVariable String name,
            @PathVariable String endpointName,
            @RequestParam(name = "v", required = false) String apiVersion) {
        log.info("getting ko endpoint " + naan + "/" + name);
        List<EndpointResource> resources = new ArrayList<>();
        if (apiVersion == null) {
            List<Endpoint> endpoints = endpointHelper.getAllVersions(naan, name, endpointName);
            for (Endpoint endpoint : endpoints) {
                resources.add(new EndpointResource(endpoint, shelfRoot));
            }
        } else {
            URI id = endpointHelper.createEndpointId(naan, name, apiVersion, endpointName);
            Endpoint endpoint = endpoints.get(id);
            if (endpoint == null) {
                throw new ActivatorException("Cannot find endpoint with id " + id);
            }
            resources.add(new EndpointResource(endpoint, shelfRoot));
        }
        return resources;
    }

}

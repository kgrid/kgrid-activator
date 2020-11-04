package org.kgrid.activator.controller;

import org.kgrid.activator.ActivatorException;
import org.kgrid.activator.EndPointResult;
import org.kgrid.activator.services.ActivationService;
import org.kgrid.activator.services.Endpoint;
import org.kgrid.adapter.api.AdapterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@RestController
@CrossOrigin
public class EndpointController extends ActivatorExceptionHandler {

    @Autowired
    private ActivationService activationService;

    @Autowired
    private Map<URI, Endpoint> endpoints;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @GetMapping(value = "/endpoints", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EndpointResource> findAllEndpoints() {
        log.info("find all endpoints");
        List<EndpointResource> resources = new ArrayList<>();

        endpoints.forEach((s, endpoint) -> {
            EndpointResource resource = new EndpointResource(endpoint);

            resources.add(resource);
        });

        return resources;
    }

    @GetMapping(value = "/endpoints/{naan}/{name}/{version}/{endpointName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public EndpointResource findEndpointOldVersion(
            @PathVariable String naan,
            @PathVariable String name,
            @PathVariable String version,
            @PathVariable String endpointName) {

        log.info("getting ko endpoint " + naan + "/" + name);

        URI id = URI.create(String.format("%s/%s/%s/%s", naan, name, version, endpointName));

        Endpoint endpoint = endpoints.get(id);

        if (endpoint == null) {
            throw new ActivatorException("Cannot find endpoint with id " + id);
        }

        return new EndpointResource(endpoint);
    }

    @GetMapping(value = "/endpoints/{naan}/{name}/{endpointName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public EndpointResource findEndpoint(
            @PathVariable String naan,
            @PathVariable String name,
            @PathVariable String endpointName,
            @RequestParam(name = "v", required = false) String version) {

        log.info("getting ko endpoint " + naan + "/" + name);

        URI id = URI.create(String.format("%s/%s/%s/%s", naan, name, version, endpointName));

        Endpoint endpoint = null;
        if (version == null) {
            for (Entry<URI, Endpoint> entry : endpoints.entrySet()) {
                if (entry.getValue().getNaan().equals(naan)
                        && entry.getValue().getName().equals(name)
                        && entry.getValue().getEndpointName().equals("/" + endpointName)) {
                    endpoint = entry.getValue();
                    break;
                }
            }
        } else {
            endpoint = endpoints.get(id);
        }

        if (endpoint == null) {
            throw new ActivatorException("Cannot find endpoint with id " + id);
        }
        return new EndpointResource(endpoint);

    }

    @PostMapping(
            value = {"/{naan}/{name}/{apiVersion}/{endpoint}"},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Object executeEndpointOldVersion(
            @PathVariable String naan,
            @PathVariable String name,
            @PathVariable String apiVersion,
            @PathVariable String endpoint,
            @RequestBody String inputs,
            @RequestHeader Map<String, String> headers) {
        return executeEndpointWithContentHeader(naan, name, endpoint, apiVersion, inputs, headers);
    }

    @PostMapping(
            value = {"/{naan}/{name}/{endpoint}"},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Object executeEndpoint(
            @PathVariable String naan,
            @PathVariable String name,
            @RequestParam(name = "v", required = false) String apiVersion,
            @PathVariable String endpoint,
            @RequestBody String inputs,
            @RequestHeader Map<String, String> headers) {
        return executeEndpointWithContentHeader(naan, name, endpoint, apiVersion, inputs, headers);
    }

    private EndPointResult executeEndpointWithContentHeader(String naan, String name, String endpoint, String apiVersion, String inputs, Map<String, String> headers) {
        URI endpointId = URI.create(String.format("%s/%s/%s/%s", naan, name, apiVersion, endpoint));

        String contentHeader = headers.get("Content-Type");
        if (contentHeader == null) {
            contentHeader = headers.get("content-type");
        }

        try {
            return activationService.execute(endpointId, inputs, contentHeader);
        } catch (AdapterException e) {
            log.error("Exception " + e);
            throw new ActivatorException("Exception for endpoint " + endpointId + " " + e.getMessage(), e);
        }
    }
}

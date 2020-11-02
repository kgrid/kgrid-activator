package org.kgrid.activator.controller;

import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;

import org.kgrid.activator.ActivatorException;
import org.kgrid.activator.EndPointResult;
import org.kgrid.activator.services.ActivationService;
import org.kgrid.activator.services.Endpoint;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.shelf.controller.KnowledgeObjectController;
import org.kgrid.shelf.domain.KoFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RestController
@CrossOrigin
public class EndpointController extends ActivatorExceptionHandler{

    @Autowired
    private ActivationService activationService;

    @Autowired
    private Map<URI, Endpoint> endpoints;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @GetMapping(value = "/endpoints", produces = MediaType.APPLICATION_JSON_VALUE)
    public EndpointResources findAllEndpoints() {
        log.info("find all endpoints");
        EndpointResources resources = new EndpointResources();

        endpoints.forEach((s, endpoint) -> {
            EndpointResource resource = createEndpointResource(endpoint);

            resources.addEndpointResource(resource);
        });

        resources.add(linkTo(EndpointController.class).withSelfRel());
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

        EndpointResource resource = createEndpointResource(endpoint);

        return resource;
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
        EndpointResource resource = createEndpointResource(endpoint);

        return resource;
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
        if (contentHeader == null){
            contentHeader = headers.get("content-type");
        }

        try {
            return activationService.execute(endpointId, inputs, contentHeader);
        } catch (AdapterException e) {
            log.error("Exception " + e);
            throw new ActivatorException("Exception for endpoint " + endpointId + " " + e.getMessage(), e);
        }
    }

    private EndpointResource createEndpointResource(Endpoint endpoint) {
        EndpointResource resource = new EndpointResource(endpoint);
        JsonNode metadata = endpoint.getMetadata();
        try {
            Link self = linkTo(EndpointController.class).slash("endpoints")
                    .slash(resource.getEndpointPath()).withSelfRel();

            Link swaggerEditor = new Link("https://editor.swagger.io?url=" +
                    linkTo(KnowledgeObjectController.class)
                            .slash("kos")
                            .slash(endpoint.getNaan())
                            .slash(endpoint.getName())
                            .slash(metadata.get("version").asText())
                            .slash(metadata.get(KoFields.SERVICE_SPEC_TERM.asStr()).asText()),
                    "swagger_editor");

            resource.add(self);
            resource.add(swaggerEditor);
        } catch (Exception e) {
            endpoint.setStatus("Could not create Endpoint Resource from malformed endpoint: " + e.getMessage());
            resource = new EndpointResource(endpoint);
        }
        return resource;
    }
}

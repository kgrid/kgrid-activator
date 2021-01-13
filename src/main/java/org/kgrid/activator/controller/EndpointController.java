package org.kgrid.activator.controller;

import org.apache.commons.lang3.StringUtils;
import org.kgrid.activator.EndPointResult;
import org.kgrid.activator.exceptions.ActivatorException;
import org.kgrid.activator.services.ActivationService;
import org.kgrid.activator.services.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
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

    @Value(("${kgrid.shelf.endpoint:kos}"))
    String shelfRoot;

    @Autowired
    MimetypesFileTypeMap fileTypeMap;

    @GetMapping(value = "/endpoints", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EndpointResource> findAllEndpoints() {
        log.info("find all endpoints");
        List<EndpointResource> resources = new ArrayList<>();

        endpoints.forEach((s, endpoint) -> {
            EndpointResource resource = new EndpointResource(endpoint, shelfRoot);

            resources.add(resource);
        });

        return resources;
    }

    @GetMapping(value = "/endpoints/{engine}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EndpointResource> findEndpointsForEngine(@PathVariable String engine) {
        log.info("find all endpoints for engine " + engine);
        List<EndpointResource> resources = new ArrayList<>();

        endpoints.forEach((s, endpoint) -> {
            EndpointResource resource = new EndpointResource(endpoint, shelfRoot);
            if (engine.equals(resource.getEngine())) {
                resources.add(resource);
            }
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
            version = getDefaultVersion(naan, name, endpointName);
        }
        URI id = getEndpointId(naan, name, version, endpointName);
        Endpoint endpoint = endpoints.get(id);
        if (endpoint == null) {
            throw new ActivatorException("Cannot find endpoint with id " + id);
        }
        return new EndpointResource(endpoint, shelfRoot);
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
            @RequestHeader HttpHeaders headers) {
        URI endpointId = getEndpointId(naan, name, apiVersion, endpoint);
        return executeEndpoint(endpointId, inputs, HttpMethod.POST, headers);
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
            @RequestHeader HttpHeaders headers) {

        if (apiVersion == null) {
            apiVersion = getDefaultVersion(naan, name, endpoint);
        }
        URI endpointId = getEndpointId(naan, name, apiVersion, endpoint);
        return executeEndpoint(endpointId, inputs, HttpMethod.POST, headers);
    }

    private String getDefaultVersion(String naan, String name, String endpoint) {
        String defaultVersion = null;
        for (Entry<URI, Endpoint> entry : endpoints.entrySet()) {
            if (entry.getValue().getNaan().equals(naan)
                    && entry.getValue().getName().equals(name)
                    && entry.getValue().getEndpointName().equals(endpoint)) {

                defaultVersion = entry.getValue().getApiVersion();
                break;
            }
        }
        return defaultVersion;
    }

    @GetMapping(
            value = {"/resource/{naan}/{name}/{endpoint}"},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Object retrieveEndpoint(
            @PathVariable String naan,
            @PathVariable String name,
            @RequestParam(name = "v", required = false) String apiVersion,
            @PathVariable String endpoint,
            @RequestHeader HttpHeaders headers) {
        URI endpointId = getEndpointId(naan, name, apiVersion, endpoint);
        return executeEndpoint(endpointId, null, HttpMethod.GET, headers);
    }

    @GetMapping(
            value = {"/resource/{naan}/{name}/{endpoint}/**"},
            produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> retrieveEndpoint(
            @PathVariable String naan,
            @PathVariable String name,
            @RequestParam(name = "v", required = false) String apiVersion,
            @PathVariable String endpoint,
            @RequestHeader HttpHeaders headers,
            HttpServletRequest request) {
        String artifactName = StringUtils.substringAfterLast(request.getRequestURI().substring(1), endpoint + "/");
        endpoint = endpoint + "/" + artifactName;
        URI endpointId = getEndpointId(naan, name, apiVersion, endpoint);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", getContentType(artifactName));
        responseHeaders.add("Content-Disposition", getContentDisposition(artifactName));
        return new ResponseEntity<>(new InputStreamResource(
                (InputStream) executeEndpoint(endpointId, artifactName, HttpMethod.GET, headers).getResult()),
                responseHeaders, HttpStatus.OK);
    }

    public String getContentType(String artifactName) {
        return fileTypeMap.getContentType(artifactName);
    }

    public String getContentDisposition(String artifactName) {
        String filename =
                artifactName.contains("/") ? StringUtils.substringAfterLast(artifactName, "/") : artifactName;
        return "inline; filename=\"" + filename + "\"";
    }

    private EndPointResult executeEndpoint(URI endpointId, String inputs, HttpMethod method, HttpHeaders headers) {
        return activationService.execute(endpointId, inputs, method, headers.getContentType());
    }

    private URI getEndpointId(String naan, String name, String apiVersion, String endpoint) {
        return URI.create(String.format("%s/%s/%s/%s", naan, name, apiVersion, endpoint));
    }
}

package org.kgrid.activator.controller;

import org.apache.commons.lang3.StringUtils;
import org.kgrid.activator.EndPointResult;
import org.kgrid.activator.Utilities.EndpointHelper;
import org.kgrid.activator.exceptions.ActivatorEndpointNotFoundException;
import org.kgrid.activator.exceptions.ActivatorUnsupportedMediaTypeException;
import org.kgrid.activator.services.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
public class RequestController extends ActivatorExceptionHandler {

    @Autowired
    private EndpointHelper endpointHelper;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value(("${kgrid.shelf.endpoint:kos}"))
    String shelfRoot;

    @PostMapping(
            value = {"/{naan}/{name}/{endpoint}"},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public EndPointResult executeEndpointQueryVersion(
            @PathVariable String naan,
            @PathVariable String name,
            @RequestParam(name = "v", required = false) String apiVersion,
            @PathVariable String endpoint,
            @RequestBody String inputs,
            @RequestHeader HttpHeaders headers) {

        if (apiVersion == null) {
            apiVersion = endpointHelper.getDefaultVersion(naan, name, endpoint);
        }
        URI endpointId = endpointHelper.createEndpointId(naan, name, apiVersion, endpoint);
        return executeEndpoint(endpointId, inputs, HttpMethod.POST, headers);
    }

    @PostMapping(
            value = {"/{naan}/{name}/{apiVersion}/{endpoint}"},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public EndPointResult executeEndpointPathVersion(
            @PathVariable String naan,
            @PathVariable String name,
            @PathVariable String apiVersion,
            @PathVariable String endpoint,
            @RequestBody String inputs,
            @RequestHeader HttpHeaders headers) {
        URI endpointId = endpointHelper.createEndpointId(naan, name, apiVersion, endpoint);
        return executeEndpoint(endpointId, inputs, HttpMethod.POST, headers);
    }

    @GetMapping(
            value = {"/{naan}/{name}/{endpoint}"},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public EndPointResult getResourceEndpoints(
            @PathVariable String naan,
            @PathVariable String name,
            @RequestParam(name = "v", required = false) String apiVersion,
            @PathVariable String endpoint,
            @RequestHeader HttpHeaders headers) {
        URI endpointId = endpointHelper.createEndpointId(naan, name, apiVersion, endpoint);
        return executeEndpoint(endpointId, null, HttpMethod.GET, headers);
    }

    @GetMapping(
            value = {"/{naan}/{name}/{endpoint}/**"},
            produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> executeResourceEndpoint(
            @PathVariable String naan,
            @PathVariable String name,
            @RequestParam(name = "v", required = false) String apiVersion,
            @PathVariable String endpoint,
            @RequestHeader HttpHeaders headers,
            HttpServletRequest request) {
        String artifactName = StringUtils.substringAfterLast(request.getRequestURI().substring(1), endpoint + "/");
        URI endpointId = endpointHelper.createEndpointId(naan, name, apiVersion, endpoint);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", endpointHelper.getContentType(artifactName));
        responseHeaders.add("Content-Disposition", endpointHelper.getContentDisposition(artifactName));
        return new ResponseEntity<>(new InputStreamResource(
                (InputStream) executeEndpoint(endpointId, artifactName, HttpMethod.GET, headers).getResult()),
                responseHeaders, HttpStatus.OK);
    }

    private EndPointResult executeEndpoint(URI endpointId, String inputs, HttpMethod method, HttpHeaders headers) {
        Endpoint endpoint = endpointHelper.getEndpoint(endpointId);
        MediaType contentType = headers.getContentType();
        if (null == endpoint || !endpoint.isActive()) {
            String[] idParts = endpointId.toString().split("/");
            List<Endpoint> versions = endpointHelper.getAllVersions(idParts[0], idParts[1], idParts[3]);
            if (!versions.isEmpty()) {
                throw new ActivatorEndpointNotFoundException("No active endpoint found for " + endpointId +
                        " Try one of these available versions: " + versions.stream().map(Endpoint::getApiVersion)
                        .collect(Collectors.joining(", ")));
            }
            throw new ActivatorEndpointNotFoundException("No active endpoint found for " + endpointId);
        }
        if (method == HttpMethod.POST) {
            validateContentType(contentType, endpoint);
        }
        return endpoint.execute(inputs, contentType);
    }

    private void validateContentType(MediaType contentType, Endpoint endpoint) {
        if (!endpoint.isSupportedContentType(contentType)) {
            throw new ActivatorUnsupportedMediaTypeException(
                    String.format("Endpoint %s does not support media type %s. Supported Content Types: %s",
                            endpoint.getId(), contentType, endpoint.getSupportedContentTypes()));
        }
    }
}

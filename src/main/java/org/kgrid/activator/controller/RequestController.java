package org.kgrid.activator.controller;

import org.apache.commons.lang3.StringUtils;
import org.kgrid.activator.EndPointResult;
import org.kgrid.activator.exceptions.ActivatorEndpointNotFoundException;
import org.kgrid.activator.exceptions.ActivatorUnsupportedMediaTypeException;
import org.kgrid.activator.services.Endpoint;
import org.kgrid.activator.utilities.EndpointHelper;
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
    public EndPointResult getResourceEndpoints(
            @PathVariable String naan,
            @PathVariable String name,
            @RequestParam(name = "v", required = false) String apiVersion,
            @PathVariable String endpoint,
            @RequestHeader HttpHeaders headers) {
        URI endpointId = endpointHelper.createEndpointId(naan, name, apiVersion, endpoint);
        return executeEndpoint(endpointId, null, HttpMethod.GET, headers);
    }

    @GetMapping(value = {"/{naan}/{name}/{endpoint}/**"}, produces = MediaType.ALL_VALUE)
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
        final String contentType = endpointHelper.getContentType(artifactName);
        if (!isValidAcceptType(headers, contentType)) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, contentType);
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, endpointHelper.getContentDisposition(artifactName));
        return new ResponseEntity<>(new InputStreamResource(
                (InputStream) executeEndpoint(endpointId, artifactName, HttpMethod.GET, headers).getResult()),
                responseHeaders, HttpStatus.OK);
    }

    private boolean isValidAcceptType(HttpHeaders headers, String contentType) {
        if (!headers.containsKey("Accept")) {
            return true;
        }
        for (MediaType acceptType : headers.getAccept()) {
            if (acceptType.isCompatibleWith(MediaType.parseMediaType(contentType))) {
                return true;
            }
        }
        return false;
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

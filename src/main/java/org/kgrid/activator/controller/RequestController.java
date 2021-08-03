package org.kgrid.activator.controller;

import org.apache.commons.lang3.StringUtils;
import org.kgrid.activator.domain.EndPointResult;
import org.kgrid.activator.domain.Endpoint;
import org.kgrid.activator.exceptions.ActivatorEndpointNotFoundException;
import org.kgrid.activator.exceptions.ActivatorUnsupportedMediaTypeException;
import org.kgrid.activator.services.ActivationService;
import org.kgrid.adapter.api.AdapterResponse;
import org.kgrid.adapter.api.ClientRequest;
import org.kgrid.adapter.api.ClientRequestBuilder;
import org.kgrid.adapter.api.Executor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.kgrid.activator.constants.CustomHeaders.ACCEPT_JSON_MINIMAL;

@RestController
@CrossOrigin
public class RequestController extends ActivatorExceptionHandler {


    @Value(("/kos"))
    String shelfRoot;

    @Autowired
    private MimetypesFileTypeMap fileTypeMap;

    @Autowired
    private ActivationService activationService;

    @PostMapping(
            value = {"/{naan}/{name}/{endpoint}"},
            produces = {MediaType.APPLICATION_JSON_VALUE, "application/json;profile=\"minimal\""})
    @ResponseStatus(HttpStatus.OK)
    public Object executeEndpointQueryVersion(
            @PathVariable String naan,
            @PathVariable String name,
            @RequestParam(name = "v", required = false) String apiVersion,
            @PathVariable String endpoint,
            @RequestBody Object inputs,
            @RequestHeader HttpHeaders headers,
            HttpServletRequest request) {
        ClientRequestBuilder clientRequestBuilder = new ClientRequestBuilder();
        ClientRequest clientRequest = clientRequestBuilder
                .body(inputs)
                .headers(headers.toSingleValueMap())
                .url(URI.create(request.getRequestURL().toString()))
                .httpMethod(request.getMethod())
                .build();
        Endpoint ep = activationService.getDefaultEndpoint(naan, name, apiVersion, endpoint);
        return executeEndpoint(ep, clientRequest);
    }

    @PostMapping(
            value = {"/{naan}/{name}/{apiVersion}/{endpoint}"},
            produces = {MediaType.APPLICATION_JSON_VALUE, "application/json;profile=\"minimal\""})
    @ResponseStatus(HttpStatus.OK)
    public Object executeEndpointPathVersion(
            @PathVariable String naan,
            @PathVariable String name,
            @PathVariable String apiVersion,
            @PathVariable String endpoint,
            @RequestBody Object inputs,
            @RequestHeader HttpHeaders headers,
            HttpServletRequest request) {
        ClientRequestBuilder clientRequestBuilder = new ClientRequestBuilder();
        ClientRequest clientRequest = clientRequestBuilder
                .body(inputs)
                .headers(headers.toSingleValueMap())
                .url(URI.create(request.getRequestURL().toString()))
                .httpMethod(request.getMethod())
                .build();
        Endpoint ep = activationService.getDefaultEndpoint(naan, name, apiVersion, endpoint);
        return executeEndpoint(ep, clientRequest);
    }

    @GetMapping(
            value = {"/{naan}/{name}/{endpoint}"},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public Object getResourceEndpoint(
            @PathVariable String naan,
            @PathVariable String name,
            @RequestParam(name = "v", required = false) String apiVersion,
            @PathVariable String endpoint,
            @RequestHeader HttpHeaders headers,
            HttpServletRequest request) {
        ClientRequestBuilder clientRequestBuilder = new ClientRequestBuilder();
        ClientRequest clientRequest = clientRequestBuilder
                .headers(headers.toSingleValueMap())
                .url(URI.create(request.getRequestURL().toString()))
                .httpMethod(request.getMethod())
                .build();
        Endpoint ep = activationService.getDefaultEndpoint(naan, name, apiVersion, endpoint);
        return executeEndpoint(ep, clientRequest);
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
        ClientRequestBuilder clientRequestBuilder = new ClientRequestBuilder();
        ClientRequest clientRequest = clientRequestBuilder
                .body(artifactName)
                .headers(headers.toSingleValueMap())
                .url(URI.create(request.getRequestURL().toString()))
                .httpMethod(request.getMethod())
                .build();

        HttpHeaders responseHeaders = new HttpHeaders();
        final String contentType = getContentType(artifactName);
        if (!isValidAcceptType(headers, contentType)) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, contentType);
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, getContentDisposition(artifactName));

        Endpoint ep = activationService.getDefaultEndpoint(naan, name, apiVersion, endpoint);
        EndPointResult executionResult = (EndPointResult) getExecutionResult(ep, clientRequest);
        return new ResponseEntity<>(new InputStreamResource(
                (InputStream) executionResult.getResult()),
                responseHeaders, HttpStatus.OK);
    }

    private boolean isValidAcceptType(HttpHeaders headers, String contentType) {
        if (!headers.containsKey("accept")) {
            return true;
        }
        for (MediaType acceptType : headers.getAccept()) {
            if (acceptType.isCompatibleWith(MediaType.parseMediaType(contentType))) {
                return true;
            }
        }
        return false;
    }

    private Object executeEndpoint(Endpoint endpoint, ClientRequest clientRequest) {
        if (!endpoint.isActive()) {
            String[] idParts = endpoint.getId().toString().split("/");
            List<Endpoint> versions = activationService.getAllVersions(idParts[0], idParts[1], idParts[3]);
            if (!versions.isEmpty()) {
                throw new ActivatorEndpointNotFoundException(endpoint.getId(),
                        versions.stream().map(Endpoint::getApiVersion).collect(Collectors.joining(",")));
            }
        }
        if(clientRequest.getHeaders().containsKey("content-type")){
            MediaType contentType = MediaType.valueOf(clientRequest.getHeaders().get("content-type"));
            if (clientRequest.getHttpMethod().equals(HttpMethod.POST.name())) {
                validateContentType(contentType, Objects.requireNonNull(endpoint));
            }
        }
        return getExecutionResult(endpoint, clientRequest);
    }

    private Object getExecutionResult(Endpoint endpoint, ClientRequest clientRequest) {
        Executor executor = endpoint.getExecutor();
        if (null == executor) {
            throw new ActivatorEndpointNotFoundException("No executor found for " + endpoint.getId());
        }

        Object result = executor.execute(clientRequest);
        if (clientRequest.getHeaders().get("accept").contains(ACCEPT_JSON_MINIMAL.getValue().toString())) {
            return result;
        }
        AdapterResponse<Object> response = new AdapterResponse<>(result, null, endpoint.getMetadata());
        return new EndPointResult<>(clientRequest, response);
    }

    private void validateContentType(MediaType contentType, Endpoint endpoint) {
        if (!endpoint.isSupportedContentType(contentType)) {
            throw new ActivatorUnsupportedMediaTypeException(
                    String.format("Endpoint %s does not support media type %s. Supported Content Types: %s",
                            endpoint.getId(), contentType, endpoint.getSupportedContentTypes()));
        }
    }

    private String getContentType(String artifactName) {
        return fileTypeMap.getContentType(artifactName);
    }

    private String getContentDisposition(String artifactName) {
        String filename =
                artifactName.contains("/") ? StringUtils.substringAfterLast(artifactName, "/") : artifactName;
        return "inline; filename=\"" + filename + "\"";
    }
}

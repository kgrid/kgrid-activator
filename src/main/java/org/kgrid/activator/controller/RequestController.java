package org.kgrid.activator.controller;

import org.apache.commons.lang3.StringUtils;
import org.kgrid.activator.domain.EndPointResult;
import org.kgrid.activator.domain.Endpoint;
import org.kgrid.activator.exceptions.ActivatorEndpointNotFoundException;
import org.kgrid.activator.services.ActivationService;
import org.kgrid.adapter.api.ClientRequest;
import org.kgrid.adapter.api.ClientRequestBuilder;
import org.kgrid.adapter.api.Executor;
import org.kgrid.adapter.api.ExecutorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.activation.MimetypesFileTypeMap;
import java.io.InputStream;
import java.util.Objects;

import static org.kgrid.activator.constants.CustomProfiles.*;

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
            value = {"/{naan}/{name}/{endpoint}"}
    )
    @ResponseStatus(HttpStatus.OK)
    public Object executeEndpointQueryVersion(
            @PathVariable String naan,
            @PathVariable String name,
            @RequestParam(name = "v", required = false) String apiVersion,
            @PathVariable String endpoint,
            RequestEntity<Object> request) {
        return executeEndpointPathVersion(naan, name, apiVersion, endpoint, request);
    }

    @PostMapping(
            value = {"/{naan}/{name}/{apiVersion}/{endpoint}"}
    )
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> executeEndpointPathVersion(
            @PathVariable String naan,
            @PathVariable String name,
            @PathVariable String apiVersion,
            @PathVariable String endpoint,
            RequestEntity<Object> request) {

        Endpoint ep = activationService.getDefaultEndpoint(naan, name, apiVersion, endpoint);
        ResponseEntity<Object> resp = new ResponseEntity<>(getExecutionResult(ep, request), HttpStatus.OK);
        return resp;
    }

    @GetMapping(
            value = {"/{naan}/{name}/{endpoint}"},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public Object getResourceEndpoint(
            @PathVariable String naan,
            @PathVariable String name,
            @RequestParam(name = "v", required = false) String apiVersion,
            @PathVariable String endpoint,
            RequestEntity<Object> request) {

        Endpoint ep = activationService.getDefaultEndpoint(naan, name, apiVersion, endpoint);
        return getExecutionResult(ep, request);
    }

    @GetMapping(value = {"/{naan}/{name}/{endpoint}/**"}, produces = MediaType.ALL_VALUE)
    public ResponseEntity<Object> executeResourceEndpoint(
            @PathVariable String naan,
            @PathVariable String name,
            @RequestParam(name = "v", required = false) String apiVersion,
            @PathVariable String endpoint,
            RequestEntity<Object> request) {
        String artifactName = StringUtils.substringAfterLast(
                request.getUrl().getPath().substring(1), endpoint + "/");

        HttpHeaders responseHeaders = new HttpHeaders();
        final String contentType = getContentType(artifactName);

        responseHeaders.add(HttpHeaders.CONTENT_TYPE, contentType);
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, getContentDisposition(artifactName));

        Endpoint ep = activationService.getDefaultEndpoint(naan, name, apiVersion, endpoint);
        EndPointResult executionResult = (EndPointResult) getExecutionResult(ep, request);
        return new ResponseEntity<>(new InputStreamResource(
                (InputStream) executionResult.getResult()),
                responseHeaders, HttpStatus.OK);
    }

    private Object getExecutionResult(Endpoint endpoint, RequestEntity<Object> request) {
        Executor executor = endpoint.getExecutor();
        if (!endpoint.isActive() || null == executor) {
            throw new ActivatorEndpointNotFoundException("No executor found for " + endpoint.getId());
        }

        ClientRequest clientRequest = new ClientRequestBuilder()
                .body(request.getBody())
                .headers(request.getHeaders())
                .url(request.getUrl())
                .httpMethod(request.getMethod().toString())
                .build();
        ExecutorResponse executorResult = executor.execute(clientRequest);
        if (request.getHeaders().getAccept().stream().anyMatch(mediaType ->
                Objects.equals(mediaType.getParameter(PROFILE.getValue()), PROFILE_MINIMAL.getValue()))) {
            return executorResult.getBody();
        }
        EndPointResult<Object> endPointResult = new EndPointResult<>(executorResult.getBody());
        endPointResult.getInfo().put("endpoint", endpoint.getMetadata());
        endPointResult.getInfo().put("inputs", clientRequest.getBody());
        return endPointResult;
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

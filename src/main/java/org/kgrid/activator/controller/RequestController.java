package org.kgrid.activator.controller;

import org.apache.commons.lang3.StringUtils;
import org.kgrid.activator.EndPointResult;
import org.kgrid.activator.Utilities.EndpointHelper;
import org.kgrid.activator.services.ActivationService;
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

@RestController
@CrossOrigin
public class RequestController {

    @Autowired
    private ActivationService activationService;

    @Autowired
    private EndpointHelper endpointHelper;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value(("${kgrid.shelf.endpoint:kos}"))
    String shelfRoot;

    @Autowired
    MimetypesFileTypeMap fileTypeMap;

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
        URI endpointId = endpointHelper.createEndpointId(naan, name, apiVersion, endpoint);
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
            apiVersion = endpointHelper.getDefaultVersion(naan, name, endpoint);
        }
        URI endpointId = endpointHelper.createEndpointId(naan, name, apiVersion, endpoint);
        return executeEndpoint(endpointId, inputs, HttpMethod.POST, headers);
    }

    @GetMapping(
            value = {"/resource/{naan}/{name}/{endpoint}"},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Object getAvailableResourceEndpoints(
            @PathVariable String naan,
            @PathVariable String name,
            @RequestParam(name = "v", required = false) String apiVersion,
            @PathVariable String endpoint,
            @RequestHeader HttpHeaders headers) {
        URI endpointId = endpointHelper.createEndpointId(naan, name, apiVersion, endpoint);
        return executeEndpoint(endpointId, null, HttpMethod.GET, headers);
    }

    @GetMapping(
            value = {"/resource/{naan}/{name}/{endpoint}/**"},
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
        endpoint = endpoint + "/" + artifactName;
        URI endpointId = endpointHelper.createEndpointId(naan, name, apiVersion, endpoint);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", endpointHelper.getContentType(artifactName));
        responseHeaders.add("Content-Disposition", endpointHelper.getContentDisposition(artifactName));
        return new ResponseEntity<>(new InputStreamResource(
                (InputStream) executeEndpoint(endpointId, artifactName, HttpMethod.GET, headers).getResult()),
                responseHeaders, HttpStatus.OK);
    }

    private EndPointResult executeEndpoint(URI endpointId, String inputs, HttpMethod method, HttpHeaders headers) {
        return activationService.execute(endpointId, inputs, method, headers.getContentType());
    }

}

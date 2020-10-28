package org.kgrid.activator.controller;

import org.kgrid.activator.ActivatorException;
import org.kgrid.activator.services.ActivationService;
import org.kgrid.adapter.api.AdapterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
@Primary
public class ActivationController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ActivationService activationService;

    @PostMapping(
            value = {"/{naan}/{name}/{apiVersion}/{endpoint}"},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Object processWithBareJsonInputOutputOldVersion(
            @PathVariable String naan,
            @PathVariable String name,
            @PathVariable String endpoint,
            @PathVariable String apiVersion,
            @RequestBody String inputs,
            @RequestHeader Map<String, String> headers) {


        URI endpointId = URI.create(String.format("%s/%s/%s/%s", naan, name, apiVersion, endpoint));
        String contentHeader = headers.get("content-type");
        if(contentHeader == null) { // Check for this because the test mockmvc does it this way
            contentHeader = headers.get("Content-Type");
        }
        try {
            return activationService.execute(endpointId, inputs, contentHeader);
        } catch (AdapterException e) {
            log.error("Exception " + e.getMessage());
            throw new ActivatorException("Exception for endpoint " + endpointId + " " + e.getMessage());
        }
    }

    @PostMapping(
            value = {"/{naan}/{name}/{endpoint}"},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Object processWithBareJsonInputOutput(
            @PathVariable String naan,
            @PathVariable String name,
            @PathVariable String endpoint,
            @RequestParam(name = "v", required = false) String apiVersion,
            @RequestBody String inputs,
            @RequestHeader Map<String, String> headers) {

        URI endpointId = URI.create(String.format("%s/%s/%s/%s", naan, name, apiVersion, endpoint));
        String contentHeader = headers.get("content-type");
        if(contentHeader == null) { // Check for this because the test mockmvc does it this way
            contentHeader = headers.get("Content-Type");
        }
        try {
            return activationService.execute(endpointId, inputs, contentHeader);
        } catch (AdapterException e) {
            log.error("Exception " + e);
            throw new ActivatorException("Exception for endpoint " + endpointId + " " + e.getMessage(), e);
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralExceptions(Exception e,
                                                                       WebRequest request) {
        return new ResponseEntity<>(generateErrorMap(request, e, "Error", HttpStatus.INTERNAL_SERVER_ERROR),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Map<String, String> generateErrorMap(WebRequest request, Exception e, String title,
                                                 HttpStatus status) {
        Map<String, String> errorInfo = new HashMap<>();
        errorInfo.put("Title", title);
        errorInfo.put("Status", status.value() + " " + status.getReasonPhrase());
        errorInfo.put("Detail", e.getMessage());
        errorInfo.put("Instance", request.getDescription(false));
        errorInfo.put("Time", new Date().toString());
        return errorInfo;

    }


}

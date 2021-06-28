package org.kgrid.activator.controller;

import org.kgrid.activator.exceptions.ActivatorEndpointNotFoundException;
import org.kgrid.activator.exceptions.ActivatorException;
import org.kgrid.activator.exceptions.ActivatorUnsupportedMediaTypeException;
import org.kgrid.adapter.api.AdapterClientErrorException;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.AdapterServerErrorException;
import org.kgrid.adapter.resource.AdapterResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class ActivatorExceptionHandler {

    protected final Logger log = LoggerFactory.getLogger(getClass().getName());

    @ExceptionHandler(ActivatorException.class)
    public ResponseEntity<Map<String, String>> handleActivatorExceptions(ActivatorException e,
                                                                         WebRequest request) {
        return new ResponseEntity<>(generateErrorMapAndLog(request, e, "Error", HttpStatus.BAD_REQUEST),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ActivatorEndpointNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleActivatorEndpointNotFoundExceptions(ActivatorEndpointNotFoundException e,
                                                                                         WebRequest request) {
        return new ResponseEntity<>(generateErrorMapAndLog(request, e, "Endpoint not found", HttpStatus.NOT_FOUND),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ActivatorUnsupportedMediaTypeException.class)
    public ResponseEntity<Map<String, String>> handleActivatorUnsupportedMediaExceptions(ActivatorUnsupportedMediaTypeException e,
                                                                                         WebRequest request) {
        return new ResponseEntity<>(generateErrorMapAndLog(request, e, "Unsupported Media Type", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
                HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(AdapterException.class)
    public ResponseEntity<Map<String, String>> handleAdapterExceptions(AdapterException e,
                                                                       WebRequest request) {
        return new ResponseEntity<>(generateErrorMapAndLog(request, e, "General Adapter Exception", HttpStatus.INTERNAL_SERVER_ERROR),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AdapterResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleAdapterExceptions(AdapterResourceNotFoundException e,
                                                                       WebRequest request) {
        return new ResponseEntity<>(generateErrorMapAndLog(request, e, "Adapter Resource Not Found", HttpStatus.NOT_FOUND),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AdapterClientErrorException.class)
    public ResponseEntity<Map<String, String>> handleAdapterExceptions(AdapterClientErrorException e,
                                                                       WebRequest request) {
        return new ResponseEntity<>(generateErrorMapAndLog(request, e, "Adapter Client Error", HttpStatus.BAD_REQUEST),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AdapterServerErrorException.class)
    public ResponseEntity<Map<String, String>> handleAdapterExceptions(AdapterServerErrorException e,
                                                                       WebRequest request) {
        return new ResponseEntity<>(generateErrorMapAndLog(request, e, "Adapter Server Error", HttpStatus.INTERNAL_SERVER_ERROR),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Map<String, String>> handleUnsupportedMediaType(Exception e,
                                                                          WebRequest request) {
        return new ResponseEntity<>(generateErrorMapAndLog(request, e, "Error", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
                HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralExceptions(Exception e,
                                                                       WebRequest request) {
        return new ResponseEntity<>(generateErrorMapAndLog(request, e, "Error", HttpStatus.INTERNAL_SERVER_ERROR),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadableExceptions(Exception e,
                                                                       WebRequest request) {
        return new ResponseEntity<>(generateErrorMapAndLog(request, e, "Http Message Not Readable", HttpStatus.BAD_REQUEST),
                HttpStatus.BAD_REQUEST);
    }

    private Map<String, String> generateErrorMapAndLog(WebRequest request, Exception e, String title,
                                                       HttpStatus status) {
        log.warn(request.getDescription(false), "; ", e.getMessage(), " Cause: ", e.getClass().getSimpleName());

        Map<String, String> errorInfo = new HashMap<>();
        errorInfo.put("Title", title);
        errorInfo.put("Status", status.value() + " " + status.getReasonPhrase());
        errorInfo.put("Detail", e.getMessage());
        errorInfo.put("Instance", request.getDescription(false));
        errorInfo.put("Time", new Date().toString());
        return errorInfo;
    }
}

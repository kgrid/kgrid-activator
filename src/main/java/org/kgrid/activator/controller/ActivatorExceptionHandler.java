package org.kgrid.activator.controller;

import org.kgrid.activator.ActivatorException;
import org.kgrid.adapter.api.AdapterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
public abstract class ActivatorExceptionHandler {

    protected Logger log = LoggerFactory.getLogger(getClass().getName());


    @ExceptionHandler(ActivatorException.class)
    public ResponseEntity<Map<String, String>> handleActivatorExceptions(Exception e,
                                                                         WebRequest request) {
        return new ResponseEntity<>(generateErrorMap(request, e, "Error", HttpStatus.BAD_REQUEST),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AdapterException.class)
    public ResponseEntity<Map<String, String>> handleAdapterExceptions(Exception e,
                                                                         WebRequest request) {
        return new ResponseEntity<>(generateErrorMap(request, e, "Error", HttpStatus.BAD_REQUEST),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Map<String, String>> handleUnsupportedMediaType(Exception e,
                                                                          WebRequest request) {
        return new ResponseEntity<>(generateErrorMap(request, e, "Error", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
                HttpStatus.UNSUPPORTED_MEDIA_TYPE);
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

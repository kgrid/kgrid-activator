package org.kgrid.activator.exceptions;

public class ActivatorEndpointNotFoundException extends ActivatorException {

    public ActivatorEndpointNotFoundException(String message) {
        super(message);
    }

    public ActivatorEndpointNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

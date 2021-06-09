package org.kgrid.activator.exceptions;

import java.net.URI;

public class ActivatorEndpointNotFoundException extends ActivatorException {

    public ActivatorEndpointNotFoundException (URI id) {
        super(String.format("Can't find executor in app context for endpoint %s", id));
    }

    public ActivatorEndpointNotFoundException (URI id, String versions) {
        super(String.format("No active endpoint found for %s Try one of these available versions: %s",
            id, versions));
    }

    public ActivatorEndpointNotFoundException(String message) {
        super(message);
    }

    public ActivatorEndpointNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

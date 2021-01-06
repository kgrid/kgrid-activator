package org.kgrid.activator.exceptions;

public class ActivatorUnsupportedMediaTypeException extends ActivatorException {
    public ActivatorUnsupportedMediaTypeException(String message) {
        super(message);
    }

    public ActivatorUnsupportedMediaTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}

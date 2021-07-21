package org.kgrid.activator.constants;

import org.springframework.http.MediaType;

public enum CustomHeaders {
    ACCEPT_JSON_MINIMAL(MediaType.valueOf("application/json-minimal"));
    private MediaType value;

    CustomHeaders(MediaType value) {
        this.value = value;
    }

    public MediaType getValue() {
        return value;
    }
}

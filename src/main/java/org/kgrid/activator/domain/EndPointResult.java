package org.kgrid.activator.domain;

import org.kgrid.adapter.api.AdapterResponse;
import org.kgrid.adapter.api.ClientRequest;

import java.util.HashMap;
import java.util.Map;

public class EndPointResult<T> {

    private final T result;
    private final Map<String, Object> info;

    public EndPointResult(T result) {
        this.result = result;
        info = new HashMap<>();
    }

    public EndPointResult(ClientRequest request, AdapterResponse<T> response) {
        this.result = response.getBody();
        info = new HashMap<>();
        this.info.put("inputs", request.getBody());
        this.info.put("ko", response.getMetadata());
    }

    public T getResult() {
        return result;
    }

    public Map<String, Object> getInfo() {
        return info;
    }

}

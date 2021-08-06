package org.kgrid.activator.domain;

import java.util.HashMap;
import java.util.Map;

public class EndPointResult<T> {

    private final T result;
    private final Map<String, Object> info;

    public EndPointResult(T result) {
        this.result = result;
        info = new HashMap<>();
    }

    public T getResult() {
        return result;
    }

    public Map<String, Object> getInfo() {
        return info;
    }

}

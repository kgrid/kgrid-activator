package org.kgrid.activator.services;

import org.kgrid.activator.exceptions.ActivatorException;
import org.kgrid.adapter.api.Adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdapterResolver {

    private final Map<String, Adapter> adapterMap;

    public AdapterResolver(List<Adapter> adapters) {
        adapterMap = new HashMap<>();
        for (Adapter adapter : adapters) {
            for (String engine : adapter.getEngines()) {
                adapterMap.put(engine, adapter);
            }
        }
    }

    protected Adapter getAdapter(String engine) {
        if (!adapterMap.containsKey(engine)) {
            throw new ActivatorException("No engine found: " + engine);
        }
        return adapterMap.get(engine);
    }
}

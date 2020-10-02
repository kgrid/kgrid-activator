package org.kgrid.activator.services;

import org.kgrid.activator.ActivatorException;
import org.kgrid.adapter.api.Adapter;

import java.util.Map;

public class AdapterResolver {

    private final Map<String, Adapter> adapters;

    public AdapterResolver(Map<String, Adapter> adapters) {
        this.adapters = adapters;
    }

    protected Adapter getAdapter(String adapterType) {

        Adapter adapter = adapters.get(adapterType.toUpperCase());
        if (adapter == null) {
            throw new ActivatorException("No Adapter Found " + adapterType);
        }

        return adapter;
    }

    public Map<String, Adapter> getAdapters() {
        return adapters;
    }

    public void setAdapters(Map<String, Adapter> adapters) {
        this.adapters.clear();
        this.adapters.putAll(adapters);
    }
}

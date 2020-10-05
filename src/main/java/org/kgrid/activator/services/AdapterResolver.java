package org.kgrid.activator.services;

import org.kgrid.activator.ActivatorException;
import org.kgrid.adapter.api.Adapter;

import java.util.List;

public class AdapterResolver {

    private final List<Adapter> adapters;

    public AdapterResolver(List<Adapter> adapters) {
        this.adapters = adapters;
    }

    protected Adapter getAdapter(String adapterType) {
        Adapter resultAdapter = null;
        for (Adapter adapter : adapters) {
            if (adapter.getEngines().contains(adapterType)) {
                resultAdapter = adapter;
            }
        }
        if (resultAdapter == null) {
            throw new ActivatorException("No Adapter Found " + adapterType);
        }
        return resultAdapter;
    }
}

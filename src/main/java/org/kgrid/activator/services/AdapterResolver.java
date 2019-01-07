package org.kgrid.activator.services;

import java.util.Map;
import org.kgrid.adapter.api.Adapter;

//@Service
public class AdapterResolver {

  private final Map<String, Adapter> adapters;

  public AdapterResolver(Map<String, Adapter> adapters) {
    this.adapters = adapters;
  }

  protected Adapter getAdapter(String adapterType) {
    return adapters.get(adapterType.toUpperCase());
  }

  public Map<String, Adapter> getAdapters() {
    return adapters;
  }

  public void setAdapters(Map<String, Adapter> adapters) {
    this.adapters.clear();
    this.adapters.putAll(adapters);
  }
}

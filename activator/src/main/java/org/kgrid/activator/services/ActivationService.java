package org.kgrid.activator.services;

import java.util.HashMap;
import java.util.ServiceLoader;
import org.kgrid.adapter.api.Adapter;
import org.springframework.stereotype.Service;

@Service
public class ActivationService {

  private HashMap<String, Adapter> adapters;

  public void loadAdapters() {

    adapters = new HashMap<String, Adapter>();

    ServiceLoader<Adapter> loader = ServiceLoader.load(Adapter.class);
    for (Adapter adapter : loader) {
      adapters.put(adapter.getType(), adapter);
    }
  }


  public Adapter findAdapter(String adapterType) {
    return adapters.get(adapterType);
  }
}

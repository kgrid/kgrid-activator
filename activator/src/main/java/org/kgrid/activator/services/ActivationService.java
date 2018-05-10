package org.kgrid.activator.services;

import java.util.HashMap;
import java.util.ServiceLoader;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.AdapterSupport;
import org.kgrid.shelf.repository.FilesystemCDOStore;
import org.springframework.stereotype.Service;

@Service
public class ActivationService {

  private HashMap<String, Adapter> adapters;

  public void loadAndInitializeAdapters() {

    adapters = new HashMap<>();

    ServiceLoader<Adapter> loader = ServiceLoader.load(Adapter.class);
    for (Adapter adapter : loader) {
      initializeAdapter(adapter);
      adapters.put(adapter.getType().toUpperCase(), adapter);
    }
  }

  protected void initializeAdapter(Adapter adapter) {
    if( adapter instanceof AdapterSupport){
      ((AdapterSupport) adapter).setCdoStore(new FilesystemCDOStore(null));
    }
    adapter.initialize();
  }


  protected Adapter findAdapter(String adapterType) {
    return adapters.get(adapterType.toUpperCase());
  }
}

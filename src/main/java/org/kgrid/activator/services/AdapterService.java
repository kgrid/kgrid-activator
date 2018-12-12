package org.kgrid.activator.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.AdapterSupport;
import org.kgrid.shelf.repository.CompoundDigitalObjectStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Service;

@Service
public class AdapterService {

  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private Properties properties;

  @Autowired
  private Environment env;

  @Autowired
  private CompoundDigitalObjectStore cdoStore;

  private HashMap<String, Adapter> adapters;

  public AdapterService() {

  }

  private static Properties getProperties(Environment env) {
    Properties properties = new Properties();
    MutablePropertySources propSrcs = ((AbstractEnvironment) env).getPropertySources();
    StreamSupport.stream(propSrcs.spliterator(), false)
        .filter(ps -> ps instanceof EnumerablePropertySource)
        .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
        .flatMap(Arrays::stream)
        .forEach(propName -> properties.setProperty(propName, env.getProperty(propName)));
    return properties;
  }

  public void loadAndInitializeAdapters() {
    properties = getProperties(env);
    adapters = new HashMap<>();

    ServiceLoader<Adapter> loader = ServiceLoader.load(Adapter.class);
    for (Adapter adapter : loader) {
      initializeAdapter(adapter);
      adapters.put(adapter.getType().toUpperCase(), adapter);
    }
  }

  protected void initializeAdapter(Adapter adapter) {
    if (adapter instanceof AdapterSupport) {
      ((AdapterSupport) adapter).setCdoStore(cdoStore);
    }
    try {
      adapter.initialize(properties);
    } catch (Exception e) {
      log.error("Cannot load adapter " + adapter.getType() + " cause: " + e.getMessage());
    }
  }

  public HashMap<String, Adapter> getLoadedAdapters() {
    return adapters;
  }

  protected Adapter findAdapter(String adapterType) {
    return adapters.get(adapterType.toUpperCase());
  }


}

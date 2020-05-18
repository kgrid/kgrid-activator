package org.kgrid.activator.services;

import org.apache.commons.lang3.StringUtils;
import org.kgrid.adapter.api.ActivationContext;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.repository.CompoundDigitalObjectStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthContributorRegistry;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

@Service
public class AdapterLoader {

  private final Logger log = LoggerFactory.getLogger(getClass());
  @Autowired private Environment env;

  @Autowired private CompoundDigitalObjectStore cdoStore;

  @Autowired private HealthContributorRegistry registry;

  public AdapterResolver loadAndInitializeAdapters(Map<EndpointId, Endpoint> endpoints) {

    Map<String, Adapter> adapters = new HashMap<>();
    ServiceLoader<Adapter> loader = ServiceLoader.load(Adapter.class);
    for (Adapter adapter : loader) {
      initializeAdapter(adapter, endpoints);
      adapters.put(adapter.getType().toUpperCase(), adapter);
      registerHealthEndpoint(adapter);
    }
    return new AdapterResolver(adapters);
  }

  private void registerHealthEndpoint(Adapter adapter) {
    HealthIndicator indicator =
        () ->
            Health.status(adapter.status())
                .withDetail("type", adapter.getType())
                .withDetail("created", Instant.now())
                .build();
    try {
      registry.registerContributor(adapter.getClass().getName(), indicator);
    } catch (IllegalStateException e) {
      log.info(e.getMessage());
    }
  }

  private void initializeAdapter(Adapter adapter, Map<EndpointId, Endpoint> endpoints) {
    try {
      adapter.initialize(
          new ActivationContext() {
            @Override
            public Executor getExecutor(String key) {
              String endpoint = StringUtils.substringAfterLast(key, "/");
              ArkId ark = new ArkId(StringUtils.substringBeforeLast(key, "/"));
              EndpointId id = new EndpointId(ark, endpoint);
              if (endpoints.containsKey(id)) {
                return endpoints.get(id).getExecutor();
              } else {
                log.error(
                    "Can't find executor in app context for endpoint ",
                    key,
                    " endpoints ",
                    endpoints);
                throw new AdapterException(
                    "Can't find executor in app context for endpoint " + key);
              }
            }

            @Override
            public byte[] getBinary(String pathToBinary) {
              return cdoStore.getBinary(pathToBinary);
            }

            @Override
            public String getProperty(String key) {
              return env.getProperty(key);
            }
          });
    } catch (Exception e) {
      log.error("Cannot load adapter " + adapter.getType() + " cause: " + e.getMessage());
    }
  }
}

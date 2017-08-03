package org.uofm.ot.activator.services;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

@Component
public class AdapterHealthIndicator extends AbstractHealthIndicator {

  @Autowired
  private ActivationService service;

  @Value("${stack.adapter.path:classpath:adapters}")
  String adapterPath;

  /**
   * Shows the adapter directory and a list of the valid adapters loaded there.
   * The load adapter list method will throw an error if the directory is empty
   * @param builder
   */

  @Override
  protected void doHealthCheck(Health.Builder builder) {

    Map<String, Class> adapters = service.loadAndGetAdapterList();
    builder
        .withDetail("Adapter directory", adapterPath)
        .withDetail("Adapters", adapters.keySet())
        .up();
  }
}
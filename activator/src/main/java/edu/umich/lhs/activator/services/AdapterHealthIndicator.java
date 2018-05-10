package edu.umich.lhs.activator.services;

import edu.umich.lhs.activator.exception.ActivatorException;
import java.io.File;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

@Component
public class AdapterHealthIndicator extends AbstractHealthIndicator {

  private final HeritageActivationService service;

  @Autowired
  public AdapterHealthIndicator(HeritageActivationService service) {
    this.service = service;
  }

  /**
   * Shows the external adapter directory and a list of the valid adapters loaded internally and from the adapter directory.
   * @param builder
   */

  @Override
  protected void doHealthCheck(Health.Builder builder) {

    String adapterPath = service.getAdapterPath();
    File adapterDir = new File(adapterPath);
    Map<String, Class> adapters = service.reloadAdapterList();

    builder
        .withDetail("Adapter directory", service.getAdapterPath())
        .withDetail("Adapters", adapters.keySet());

    if (adapterPath.isEmpty()) {
      builder.withDetail("Warning", "External adapter directory is not configured").unknown();
    } else if (!adapterDir.exists() ) {
      builder.down(new ActivatorException("Cannot find external directory " + adapterPath));
    } else if (!adapterDir.isDirectory() ){
      builder.down(new ActivatorException(adapterPath + " is not a valid directory"));
    }
    else {
      builder.up();
    }
  }
}
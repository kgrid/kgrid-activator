package org.kgrid.mock.adapter;

import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;
import java.nio.file.Path;
import org.kgrid.adapter.api.ActivationContext;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.domain.ArkId;

public class MockAdapter implements Adapter {

  private String status;

  @Override
  public String getType() {
    return "MockAdapter";
  }

  @Override
  public void initialize(ActivationContext activationContext) {
    status = "UP";
  }

  @Override
  public Executor activate(URI objectLocation, String naan, String name, String version, String endpointName, JsonNode deploymentSpec) {return null;}

  @Override
  public Executor activate(URI uri, URI uri1, JsonNode jsonNode) {
    return null;
  }

  @Override
  public String status() {
    return status;
  }
}

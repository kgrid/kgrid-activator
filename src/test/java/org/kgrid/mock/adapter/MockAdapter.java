package org.kgrid.mock.adapter;

import java.nio.file.Path;
import org.kgrid.adapter.api.ActivationContext;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.Executor;

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
  public Executor activate(Path path, String s) {
    return null;
  }

  @Override
  public String status() {
    return status;
  }
}

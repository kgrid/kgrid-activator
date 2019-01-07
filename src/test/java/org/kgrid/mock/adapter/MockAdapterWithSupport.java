package org.kgrid.mock.adapter;

import java.nio.file.Path;
import java.util.Properties;
import org.kgrid.adapter.api.ActivationContext;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.AdapterSupport;
import org.kgrid.adapter.api.Executor;

public class MockAdapterWithSupport implements Adapter, AdapterSupport {
  private boolean initialized;
  private boolean cdoStore;
  private ActivationContext context;

  @Override
  public String getType() {
    return "MockAdapterWithSupport";
  }

  @Override
  public void initialize(Properties properties) {
      initialized=true;
  }

  @Override
  public Executor activate(Path path, String s) {
    return null;
  }

  @Override
  public String status() {
    return (initialized && (getContext()!=null)) ? "UP" : "DOWN";
  }

  @Override
  public void setContext(ActivationContext context) {
    this.context = context;
  }

  @Override
  public ActivationContext getContext() {
    return context;
  }
}

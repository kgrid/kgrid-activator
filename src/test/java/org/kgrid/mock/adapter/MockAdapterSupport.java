package org.kgrid.mock.adapter;

import java.nio.file.Path;
import java.util.Properties;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.AdapterSupport;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.repository.CompoundDigitalObjectStore;

public class MockAdapterSupport implements Adapter, AdapterSupport {
  private boolean initialized;
  private boolean cdoStore;
  @Override
  public String getType() {
    return "MockAdapterSupport";
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
    return (initialized && cdoStore)?"UP":"DOWN";
  }

  @Override
  public void setCdoStore(CompoundDigitalObjectStore compoundDigitalObjectStore) {
    cdoStore = true;
  }
}

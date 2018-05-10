package org.kgrid.activator.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.kgrid.adapter.api.Adapter;

public class ActivationServiceTest {

  private  ActivationService service;

  @Before
  public void setUp() {
    service = new ActivationService();
  }

  @Test
  public void loadMockAdapters(){
    service.loadAndInitializeAdapters();
    Adapter adapter = service.findAdapter("MOCKADAPTER");
    assertNotNull(adapter);
  }
  @Test
  public void adapterNotFound(){
    service.loadAndInitializeAdapters();
    assertNull(service.findAdapter("XXXXX"));
  }

  @Test
  public void loadedAdaptersAreInitialized(){
    service.loadAndInitializeAdapters();
    Adapter jsAdapter =service.findAdapter("mockadapter");
    assertEquals("UP",jsAdapter.status());

    jsAdapter =service.findAdapter("mockadaptersupport");
    assertEquals("UP",jsAdapter.status());
  }

}
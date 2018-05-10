package org.kgrid.activator.services;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.kgrid.adapter.api.Adapter;

public class ActivationServiceTest {

  private  ActivationService service;

  @Before
  public void setUp() throws Exception {
    service = new ActivationService();
  }

  @Test
  public void loadAdapters(){

    service.loadAdapters();
    Adapter adapter = service.findAdapter("JAVASCRIPT");
    assertNotNull(adapter);

  }

}
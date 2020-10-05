package org.kgrid.activator.services;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.kgrid.activator.ActivatorException;
import org.kgrid.activator.KgridActivatorApplication;
import org.kgrid.adapter.api.Adapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {KgridActivatorApplication.class})
public class AdapterLoaderTest {

  @Rule public ExpectedException thrown = ExpectedException.none();
  @Autowired AdapterResolver adapterResolver;
  @Autowired private AdapterLoader adapterLoader;

  @Test
  public void loadMockAdapters() {
    adapterLoader.loadAndInitializeAdapters(Collections.emptyMap());
    Adapter adapter = adapterResolver.getAdapter("mockadapter");
    assertNotNull(adapter);
  }

  @Test(expected = ActivatorException.class)
  public void adapterNotFound() {
    adapterLoader.loadAndInitializeAdapters(Collections.emptyMap());
    adapterResolver.getAdapter("XXXXX");
  }

  @Test
  public void loadedAdaptersAreInitialized() {
    adapterLoader.loadAndInitializeAdapters(Collections.emptyMap());
    Adapter mockAdapter = adapterResolver.getAdapter("mockadapter");
    assertEquals("UP", mockAdapter.status());
  }
}

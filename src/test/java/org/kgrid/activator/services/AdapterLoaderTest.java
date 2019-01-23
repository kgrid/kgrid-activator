package org.kgrid.activator.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.kgrid.activator.ActivatorException;
import org.kgrid.activator.KgridActivatorApplication;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {KgridActivatorApplication.class})
public class AdapterLoaderTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Autowired
  private AdapterLoader adapterLoader;

  @Autowired
  AdapterResolver adapterResolver;

  @Autowired
  KnowledgeObjectRepository knowledgeObjectRepository;

  @Test
  public void loadMockAdapters() {
    adapterLoader.loadAndInitializeAdapters();
    Adapter adapter = adapterResolver.getAdapter("MOCKADAPTER");
    assertNotNull(adapter);
  }

  @Test (expected = ActivatorException.class)
  public void adapterNotFound() {
    adapterLoader.loadAndInitializeAdapters();
    adapterResolver.getAdapter("XXXXX");
  }

  @Test
  public void loadedAdaptersAreInitialized() {
    adapterLoader.loadAndInitializeAdapters();
    Adapter jsAdapter = adapterResolver.getAdapter("mockadapter");
    assertEquals("UP", jsAdapter.status());
  }

}
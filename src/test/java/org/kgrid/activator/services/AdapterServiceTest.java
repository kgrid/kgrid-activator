package org.kgrid.activator.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.kgrid.activator.KgridActivatorApplication;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {KgridActivatorApplication.class})
public class AdapterServiceTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Autowired
  private AdapterService adapterService;

  @Autowired
  KnowledgeObjectRepository knowledgeObjectRepository;

  @Test
  public void loadMockAdapters() {
    adapterService.loadAndInitializeAdapters();
    Adapter adapter = adapterService.findAdapter("MOCKADAPTER");
    assertNotNull(adapter);
  }

  @Test
  public void adapterNotFound() {
    adapterService.loadAndInitializeAdapters();
    assertNull(adapterService.findAdapter("XXXXX"));
  }

  @Test
  public void loadedAdaptersAreInitialized() {
    adapterService.loadAndInitializeAdapters();
    Adapter jsAdapter = adapterService.findAdapter("mockadapter");
    assertEquals("UP", jsAdapter.status());

    jsAdapter = adapterService.findAdapter("mockadaptersupport");
    assertEquals("UP", jsAdapter.status());
  }

}
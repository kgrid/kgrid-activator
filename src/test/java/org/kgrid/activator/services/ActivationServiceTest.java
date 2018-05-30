package org.kgrid.activator.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kgrid.activator.KgridActivatorApplication;
import org.kgrid.activator.ActivatorException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.domain.KnowledgeObject;
import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {KgridActivatorApplication.class})
public class ActivationServiceTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Autowired
  private ActivationService service;

  @Autowired
  KnowledgeObjectRepository knowledgeObjectRepository;

  @Test
  public void loadMockAdapters() {
    service.loadAndInitializeAdapters();
    Adapter adapter = service.findAdapter("MOCKADAPTER");
    assertNotNull(adapter);
  }

  @Test
  public void adapterNotFound() {
    service.loadAndInitializeAdapters();
    assertNull(service.findAdapter("XXXXX"));
  }

  @Test
  public void loadedAdaptersAreInitialized() {
    service.loadAndInitializeAdapters();
    Adapter jsAdapter = service.findAdapter("mockadapter");
    assertEquals("UP", jsAdapter.status());

    jsAdapter = service.findAdapter("mockadaptersupport");
    assertEquals("UP", jsAdapter.status());
  }

  @Test
  public void activateKnowledgeObjects() throws IOException {

    ObjectMapper mapper = new ObjectMapper();
    String jsonInput = "{\"name\": \"Tester\"}";
    TypeReference<HashMap<String, String>> typeRef
        = new TypeReference<HashMap<String, String>>() {};
    Map<String, String> map = mapper.readValue(jsonInput, typeRef);

    service.loadAndActivateEndpoints();

    assertEquals(2, service.getEndpointExecutors().size());
    Executor executor = service.getEndpointExecutors()
        .get("99999/newko/v0.0.1/welcome");
    assertEquals("Welcome to Knowledge Grid, Tester", executor
        .execute(map));

  }

  @Test
  public void findResourcePathforKO() {
    KnowledgeObject knowledgeObject = knowledgeObjectRepository
        .findByArkIdAndVersion(new ArkId("99999-newko"), "v0.0.1");

    String resource = knowledgeObject.getMetadata().get("models").get("resource").asText();
    assertEquals("resource/welcome.js",
        resource);
  }

  @Test
  public void activateKnowledageObjectEndPoint() {

    KnowledgeObject knowledgeObject = knowledgeObjectRepository
        .findByArkIdAndVersion(new ArkId("99999-newko"), "v0.0.1");

    assertTrue(service.activateKnowledgeObjectEndPoint
        (knowledgeObject) instanceof Executor ? true : false);

  }

  @Test
  public void activateKnowledageObjectEndPointNoAdapterFound() {

    thrown.expect(ActivatorException.class);

    KnowledgeObject knowledgeObject = knowledgeObjectRepository
        .findByArkIdAndVersion(new ArkId("99999-newko"), "v0.0.0");

    service.activateKnowledgeObjectEndPoint(knowledgeObject);

  }
}
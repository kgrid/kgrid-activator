package org.kgrid.activator.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.kgrid.activator.ActivatorException;
import org.kgrid.activator.EndPoint;
import org.kgrid.activator.KgridActivatorApplication;
import org.kgrid.adapter.api.AdapterException;
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

  @Autowired
  ServiceDescriptionService serviceDescriptionService;

  @Test
  public void activateKnowledgeObjects() throws IOException {

    ObjectMapper mapper = new ObjectMapper();
    String jsonInput = "{\"name\": \"Tester\"}";
    TypeReference<HashMap<String, String>> typeRef
        = new TypeReference<HashMap<String, String>>() {};
    Map<String, String> map = mapper.readValue(jsonInput, typeRef);

    service.loadAndActivateEndPoints();

    Executor executor = service.getEndpoints()
        .get("99999/newko/v0.0.0/welcome").getExecutor();
    assertEquals("Welcome to Knowledge Grid, Tester", executor
        .execute(map));

  }

  @Test
  public void findResourcePathforKO() {
    KnowledgeObject knowledgeObject = knowledgeObjectRepository
        .findByArkIdAndVersion(new ArkId("99999-newko"), "v0.0.0");

    String resource = knowledgeObject.getModelMetadata().get("resource").asText();
    assertEquals("resource/welcome.js",
        resource);
  }

  @Test
  public void activateKnowledageObjectEndPoint() {

    KnowledgeObject knowledgeObject = knowledgeObjectRepository
        .findByArkIdAndVersion(new ArkId("99999-newko"), "v0.0.0");

    assertTrue(service.activateKnowledgeObjectEndpoint
        (knowledgeObject) instanceof EndPoint ? true : false);

  }


  @Test
  public void activateKnowledageObjectEndPointNoAdapterFound() {

    thrown.expect(ActivatorException.class);
    thrown.expectMessage("\"BADADAPTER\" adapter type found");


    KnowledgeObject knowledgeObject = knowledgeObjectRepository
        .findByArkIdAndVersion(new ArkId("99999-newko"), "v0.0.NoAdapter");

    service.activateKnowledgeObjectEndpoint(knowledgeObject);

  }

  @Test
  public void activateKnowledageObjectNoServiceDescription() {

    thrown.expect(ActivatorException.class);
    thrown.expectMessage("Service Description is Required");

    KnowledgeObject knowledgeObject = knowledgeObjectRepository
        .findByArkIdAndVersion(new ArkId("99999-newko"), "v0.0.NoServiceDescription");

    service.activateKnowledgeObjectEndpoint(knowledgeObject);

  }
  @Test
  public void activateKnowledageObjectEmptyServiceDescription() {

    thrown.expect(ActivatorException.class);
    thrown.expectMessage("Service Description is Required");

    KnowledgeObject knowledgeObject = knowledgeObjectRepository
        .findByArkIdAndVersion(new ArkId("99999-newko"), "v0.0.EmptyServiceDescription");

    service.activateKnowledgeObjectEndpoint(knowledgeObject);

  }

  @Test
  public void activateKnowledageObjectServiceDescriptionNoPaths() {

    thrown.expect(ActivatorException.class);
    thrown.expectMessage("Service Description Paths are Required");

    KnowledgeObject knowledgeObject = knowledgeObjectRepository
        .findByArkIdAndVersion(new ArkId("99999-newko"), "v0.0.ServiceDescriptionNoPaths");

    service.activateKnowledgeObjectEndpoint(knowledgeObject);

  }
  @Test
  public void activateKnowledageObjectEndPointNoFunction() {

    thrown.expect(ActivatorException.class);
    thrown.expectMessage("Function Name on Model Required");

    KnowledgeObject knowledgeObject = knowledgeObjectRepository
        .findByArkIdAndVersion(new ArkId("99999-newko"), "v0.0.Nofunction");

    service.activateKnowledgeObjectEndpoint(knowledgeObject);

  }

  @Test
  public void activateKnowledageObjectEndPointJSCompile() {

    thrown.expect(AdapterException.class);
    thrown.expectMessage("unable to compile script 99999-newko"
        + FileSystems.getDefault().getSeparator() +"v0.0.JSNotCompile");

    KnowledgeObject knowledgeObject = knowledgeObjectRepository
        .findByArkIdAndVersion(new ArkId("99999-newko"), "v0.0.JSNotCompile");

    service.activateKnowledgeObjectEndpoint(knowledgeObject);

  }

  @Test
  public void activateKnowledageObjectEndPointNoResource() {

    thrown.expect(ActivatorException.class);
    thrown.expectMessage("Resource on Model Required");

    KnowledgeObject knowledgeObject = knowledgeObjectRepository
        .findByArkIdAndVersion(new ArkId("99999-newko"), "v0.0.NoResource");

    service.activateKnowledgeObjectEndpoint(knowledgeObject);

  }
  @Test
  public void findEndPointPath() {

    KnowledgeObject knowledgeObject = knowledgeObjectRepository
        .findByArkIdAndVersion(new ArkId("99999-newko"), "v0.0.0");

    serviceDescriptionService.findPath(knowledgeObject);

  }
}
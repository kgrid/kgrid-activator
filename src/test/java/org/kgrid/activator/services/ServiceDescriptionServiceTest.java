package org.kgrid.activator.services;

import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kgrid.activator.KgridActivatorApplication;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.domain.KnowledgeObject;
import org.kgrid.shelf.repository.CompoundDigitalObjectStore;
import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {KgridActivatorApplication.class})

public class ServiceDescriptionServiceTest {

  @Autowired
  KnowledgeObjectRepository knowledgeObjectRepository;

  @Autowired
  ServiceDescriptionService service = new ServiceDescriptionService();

  @Test
  public void loadServiceDescription() {

    try{
      Path path = Paths.get("src/test/resources/servicedescription.yaml");
      byte [] openAPI = Files.readAllBytes(path);
      JsonNode serviceDescriptionwJsonNode = service.loadServiceDescription(openAPI);

      assertEquals("3.0.0",serviceDescriptionwJsonNode.get("openapi").asText());
      assertEquals("This is a sample Petstore server", serviceDescriptionwJsonNode.get("info").get("description").asText());
    } catch (Exception e){
        assertFalse("throw exception "  + e, true);
    }

  }

  @Test
  public void findPath()  {

    KnowledgeObject knowledgeObject = knowledgeObjectRepository
        .findByArkIdAndVersion(new ArkId("99999-10101"), "v0.0.1");

    JsonNode serviceDescriptionwJsonNode = service.loadServiceDescription(knowledgeObject);
    assertEquals("3.0.0",serviceDescriptionwJsonNode.get("openapi").asText());
    assertEquals("/opioidDetector",  ((ObjectNode) serviceDescriptionwJsonNode.get(ServiceDescriptionService.SERVICE_DESCRIPTION_PATHS)).fieldNames().next());



  }
}
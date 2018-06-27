package org.kgrid.activator.services;

import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class ServiceDescriptionServiceTest {

  @Test
  public void loadServiceDescription() {
    ServiceDescriptionService service = new ServiceDescriptionService();
    try{
      JsonNode serviceDescriptionwJsonNode = service.loadServiceDescription("openapi: 3.0.0 \ninfo:\n" + "  description: This is a simple API");
      assertEquals("3.0.0",serviceDescriptionwJsonNode.get("openapi").asText());
      assertEquals("This is a simple API",serviceDescriptionwJsonNode.get("info").get("description").asText());
    } catch (Exception e){
        System.out.print(e.getMessage());
    }

  }
}
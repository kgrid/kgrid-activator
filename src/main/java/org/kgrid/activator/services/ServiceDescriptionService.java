package org.kgrid.activator.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.nio.file.Paths;
import org.kgrid.shelf.domain.KnowledgeObject;
import org.kgrid.shelf.repository.CompoundDigitalObjectStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServiceDescriptionService {
  private final Logger log = LoggerFactory.getLogger(this.getClass());


  YAMLMapper yamlMapper = new YAMLMapper();

  @Autowired
  CompoundDigitalObjectStore cdoStore;

  public JsonNode loadServiceDescription(KnowledgeObject knowledgeObject){

    if(knowledgeObject.getMetadata().has("service")){
      String serviceDescriptionPath = knowledgeObject.getMetadata().get("service").asText();

      try {

        JsonNode serviceJsonNode = loadServiceDescription(
            cdoStore.getBinary(Paths.get(knowledgeObject.getArkId().getFedoraPath(),
                knowledgeObject.version(),serviceDescriptionPath)));

        return serviceJsonNode;

      } catch (Exception e) {
        log.warn(e.getMessage());
      }
    }

    return yamlMapper.createObjectNode();


  }

  public JsonNode loadServiceDescription(String serviceDescription) throws IOException {

    JsonNode jsonNode = yamlMapper.readTree(serviceDescription);

    return jsonNode;

  }

  public JsonNode loadServiceDescription(byte[] serviceDescription) throws IOException {

    JsonNode jsonNode = yamlMapper.readTree(serviceDescription);

    return jsonNode;

  }

}

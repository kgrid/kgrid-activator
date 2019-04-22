package org.kgrid.activator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.kgrid.activator.services.Endpoint;
import org.kgrid.shelf.ShelfException;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.domain.KnowledgeObject;
import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EndpointLoader {

  final Logger log = LoggerFactory.getLogger(EndpointLoader.class);

  @Autowired
  KnowledgeObjectRepository knowledgeObjectRepository;

  /**
   * Creates endpoints based on the Implementations specification.
   * If endpoint resources can not be found no endpoint will be
   * created.
   *
   * @return collection of endpoints
   */
  public Map<String, Endpoint> load(ArkId ark) {

    Map<String, Endpoint> endpoints = new HashMap<>();

    if (ark.isImplementation()) {

      log.info("ArkId: " + ark.getDashArkImplementation());
      loadKOImplemtation(ark, endpoints);

    } else {

      JsonNode knowledgeObjectMetadata = knowledgeObjectRepository.findKnowledgeObjectMetadata(ark);

      List<ArkId> implementationArkIds = getImplementationArkIds(knowledgeObjectMetadata);

      implementationArkIds.stream().forEach(arkId -> loadKOImplemtation( arkId, endpoints));

    }

    return endpoints;
  }

  /**
   *
   * @param ark
   * @param endpoints
   * @return
   */
  private boolean loadKOImplemtation(ArkId ark, Map<String, Endpoint> endpoints) {


    log.info("Load KO Implementation {}", ark.getDashArkImplementation());

    try {

      JsonNode implementationMetadata = knowledgeObjectRepository.findImplementationMetadata(ark);

      JsonNode serviceDescription = knowledgeObjectRepository
          .findServiceSpecification(ark, implementationMetadata);

      serviceDescription.get("paths").fields().forEachRemaining(service -> {

        JsonNode spec = new ObjectMapper().createObjectNode();
        try {
          JsonNode deploymentSpecification = knowledgeObjectRepository
              .findDeploymentSpecification(ark, implementationMetadata);
          spec = deploymentSpecification.get("endpoints").get(service.getKey());
        } catch (ShelfException e) {
          log.info(ark.getDashArkImplementation() + " has no deployment descriptor, looking for info in the service spec." ) ;
        }

        JsonNode post = service.getValue().get("post");
        if(post.has("x-kgrid-artifact") &&
            post.has("x-kgrid-adapterType") &&
            post.has("x-kgrid-entry")) {
          ((ObjectNode)spec).set("artifact", post.get("x-kgrid-artifact").get("value"));
          ((ObjectNode)spec).set("adapterType", post.get("x-kgrid-adapterType").get("value"));
          ((ObjectNode)spec).set("entry", post.get("x-kgrid-entry").get("value"));
        }

        final Endpoint endpoint = new Endpoint();
        endpoint.setActivated(LocalDateTime.now());
        endpoint.setPath(ark.getDashArkImplementation() + service.getKey());
        endpoint.setDeployment(spec);
        endpoint.setService(serviceDescription);
        endpoint.setImpl(implementationMetadata);
        endpoints.put(ark.getDashArkImplementation() + service.getKey(), endpoint);

      });

    } catch (ShelfException e) {
      log.warn("Cannot load " + ark.getDashArkImplementation() + ": " + e.getMessage() ) ;
      return true;
    } catch (NullPointerException ex) {
      log.warn("Cannot load " + ark.getDashArkImplementation() + ": missing required model metadata path(s) for implementation, deployment and/or service." ) ;
      return true;
    }
    return false;
  }

  /**
   * Loads all the endpoints
   *
   * @return collection of endpoints
   */
  public Map<String, Endpoint> load() {
    Map<ArkId, JsonNode> kos = knowledgeObjectRepository.findAll();
    Map<String, Endpoint> endpoints = new HashMap<>();

    for (Entry<ArkId, JsonNode> ko : kos.entrySet()) {
      List<ArkId> arks = getImplementationArkIds(ko.getValue());
      arks.forEach(arkId -> {
        endpoints.putAll(load(arkId));
      });
    }
    return endpoints;
  }

  private List<ArkId> getImplementationArkIds(JsonNode ko) {
    JsonNode implementations = ko.get(KnowledgeObject.IMPLEMENTATIONS_TERM);

    List<ArkId> arks = new ArrayList<>();
    if (implementations.isArray()) {
      implementations.elements().forEachRemaining(impl -> {
        arks.add(new ArkId(impl.asText()));
      });
    } else {
      arks.add(new ArkId(implementations.asText()));
    }
    return arks;
  }

  String getKORepoLocation(){
    return knowledgeObjectRepository.getConnection();
  }

}

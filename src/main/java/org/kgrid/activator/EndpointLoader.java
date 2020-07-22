package org.kgrid.activator;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.kgrid.activator.services.Endpoint;
import org.kgrid.activator.services.EndpointId;
import org.kgrid.shelf.domain.ArkId;
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
  public Map<EndpointId, Endpoint> load(ArkId ark) {

    Map<EndpointId, Endpoint> endpoints = new HashMap<>();

    if (ark.hasVersion()) {

      log.info("ArkId: " + ark.getDashArkVersion());

      // load required activation files for KO with `ark`
      // and create a new Endpoint and put into `endpoints` map under `/naan/name/version/endpoint`
      loadKOImplemtation(ark, endpoints);

    } else {
      JsonNode knowledgeObjectMetadata = knowledgeObjectRepository.findKnowledgeObjectMetadata(ark);
      if(knowledgeObjectMetadata.isArray()) {
        knowledgeObjectMetadata.forEach(ko -> {
          if(ko.has("version")) {
            ArkId id = new ArkId(ark.getNaan(), ark.getName(), (ko.get("version").asText()));
            loadKOImplemtation(id, endpoints);
          }
        });
      }
    }
    return endpoints;
  }

  /**
   *
   * @param ark
   * @param endpoints
   * @return
   */
  private boolean loadKOImplemtation(ArkId ark, Map<EndpointId, Endpoint> endpoints) {
    log.info("Load KO Implementation {}", ark.getDashArkVersion());
    final JsonNode koMetadata;
    final JsonNode serviceDescription;
    final JsonNode deploymentSpecification;

    try {

      // Load the parts
      koMetadata = knowledgeObjectRepository.findKnowledgeObjectMetadata(ark);

      serviceDescription = knowledgeObjectRepository
          .findServiceSpecification(ark, koMetadata);

//        if (koMetadata.get("deploymentSpecification") != null) {
//          deploymentSpecification = knowledgeObjectRepository
//              .findDeploymentSpecification(ark, koMetadata);
//        } else {
//          deploymentSpecification = null;
//        }

      JsonNode t = null;
      try {
        t = knowledgeObjectRepository
                .findDeploymentSpecification(ark, koMetadata);
      } catch (Exception e) {
        log.warn("no deployment spec found for " + ark.getSlashArkVersion());
      }
      deploymentSpecification = t;

      validateMetadata(koMetadata); //
      validateServiceDescription(serviceDescription); //
      validateDeploymentSpecification(deploymentSpecification); //

      // For each endpoint path, create an Endpoint that wraps the deployment spec for that path
      serviceDescription.get("paths").fields().forEachRemaining(path -> {

        JsonNode spec = path.getValue().get("post").get("x-kgrid-activation");

        if (spec == null && deploymentSpecification != null) {
          spec = deploymentSpecification.get("endpoints").get(path.getKey());
        }

        Endpoint endpoint = Endpoint.Builder.anEndpoint()
            .withService(serviceDescription)
            .withDeployment(spec)
            .withMetadata(koMetadata)
            .withStatus(
                (spec==null) ? "Missing deployment spec for " + path.getKey() : "GOOD")
            .withPath(ark.getSlashArk()
                + path.getKey()
                + (ark.getVersion() != null ? "?v=" + ark.getVersion(): ""))
            .build();

        endpoints.put(new EndpointId(ark, path.getKey()), endpoint);

      });

    } catch (Exception e) {
      // Log only; don't throw the ActivatorException. Just keep processing KOs.
      final ActivatorException activatorException = new ActivatorException(
          "Failed to load " + ark.getSlashArkVersion(),
          e);
      log.warn(activatorException.getMessage());
    }

    return false;
  }

  private void validateDeploymentSpecification(JsonNode deploymentSpecification) {

  }

  private void validateServiceDescription(JsonNode serviceDescription) {

  }

  private void validateMetadata(JsonNode koMetadata) {

  }

  /**
   * Loads all the endpoints
   *
   * @return collection of endpoints
   */
  public Map<EndpointId, Endpoint> load() {
    Map<ArkId, JsonNode> kos = knowledgeObjectRepository.findAll();
    Map<EndpointId, Endpoint> temp = new HashMap<>();

    for (Entry<ArkId, JsonNode> ko : kos.entrySet()) {
      temp.putAll(load(ko.getKey()));
    }

    // Putting everything in a treemap sorts them alphabetically
    TreeMap<EndpointId, Endpoint> endpoints = new TreeMap<>(Collections.reverseOrder());
    endpoints.putAll(temp);

    return endpoints;
  }

  String getKORepoLocation(){
    return knowledgeObjectRepository.getConnection();
  }

}

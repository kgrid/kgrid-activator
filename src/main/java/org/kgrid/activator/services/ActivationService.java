package org.kgrid.activator.services;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.domain.KnowledgeObject;
import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class ActivationService {

  final Logger log = LoggerFactory.getLogger(this.getClass());
  Map<String, Endpoint> endpoints = new HashMap<>();
  private KnowledgeObjectRepository knowledgeObjectRepository;

  public ActivationService(KnowledgeObjectRepository repo) {
    this.knowledgeObjectRepository = repo;
  }

  public Map<String, Endpoint> loadEndpoints() {

    Map<ArkId, JsonNode> kos = knowledgeObjectRepository.findAll();

    for (Entry<ArkId, JsonNode> ko : kos.entrySet()) {

      List<ArkId> arks = getImplementationArkIds(ko.getValue());

      for (ArkId ark : arks) {
        endpoints.putAll(loadEndpoints(ark));
      }
    }
    return endpoints;
  }

  public Map<String, Endpoint> loadEndpoints(ArkId ark) {
    log.info("ArkId: " + ark.getDashArkImplementation());

    JsonNode implementationMetadata = knowledgeObjectRepository
        .findImplementationMetadata(ark);

    JsonNode deploymentSpecification = knowledgeObjectRepository
        .findDeploymentSpecification(ark, implementationMetadata);

    JsonNode serviceDescription = knowledgeObjectRepository
        .findServiceSpecification(ark, implementationMetadata);

    Map<String, Endpoint> eps = new HashMap<>();
    serviceDescription.get("paths").fields().forEachRemaining(service -> {

      JsonNode spec = deploymentSpecification.get("endpoints").get(service.getKey());

//        String adapterType = endpointSpec.get("adapterType").asText();
//        final String payloadPath = implementationMetadata.get(KnowledgeObject.PAYLOAD_TERM).asText();
//        byte[] payload = knowledgeObjectRepository.findPayload(ark, payloadPath);

      final Endpoint endpoint = new Endpoint();
      endpoint.setDeployment(spec);
      endpoint.setService(serviceDescription);
      endpoint.setImpl(implementationMetadata);
      eps.put(ark.getDashArkImplementation() + service.getKey(), endpoint);
    });


    return eps;
  }

  private List<ArkId> getImplementationArkIds(JsonNode ko) {
    JsonNode implementations = ko.get(KnowledgeObject.IMPLEMENTATIONS_TERM);

    assert (!implementations.isNull());

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

  public Map<String, Endpoint> getEndpoints() {
    return new HashMap<>();
  }

  Endpoint activateKnowledgeObjectEndpoint(KnowledgeObject knowledgeObject)
      throws AdapterException {
    // Rename as 'activateEndpoints(ArkId, JsonNode)'
    // Accepts an ArkId and JsonNode  speccing the Implementation
    // returns a Map<ArkId, Endpoint> which can be added to the
    // master Map of Endpoints

    return new Endpoint();
  }

  protected void validateEndPoint(KnowledgeObject knowledgeObject) {
    /* Need a simple way to validate that an object can be activated
     * The ActivatorException thrown in ActivateKnowledgeObjectEndpoint
     * is used to skip objects on the shelf if they aren't valid
     */
    log.info(String.format("valid ko @ " + knowledgeObject.getArkId()));
  }

  public void startEndpointWatcher() throws IOException {
  }

  public void activate(Map<String, Endpoint> eps) {
    for ( Entry<String, Endpoint> endpointEntry : eps.entrySet() ) {

      byte[] payload = null;
      final Endpoint endpoint = endpointEntry.getValue();
      Executor executor = activate(payload, endpoint);
      endpoint.setExecutor(executor);

    }
  }

  private Executor activate(byte[] payload, Endpoint value) {
    return new Executor() {
      @Override
      public Object execute(Object input) {
        return null;
      }
    };
  }
}



package org.kgrid.activator.services;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.kgrid.activator.EndPoint;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.domain.KnowledgeObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class ActivationService extends ActivationServiceDeprecated {

  final Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public void loadAndActivateEndPoints() {
    // fetch the Map<ArkId, JsonNode> using findAll()
    // for each ko (== JsonNode) get the Implementations
    // as a collection of ArkIds, JsonNode pairs and then
    // loop through calling activateEndpoints(ArkId, JsonNode) on each Implementation
    // Add each return Map<ArkId, Endpoint> to the mater Map

    Map<ArkId, JsonNode> kos = knowledgeObjectRepository.findAll();

    for (JsonNode ko : kos.values()) {

      List<ArkId> arks = getImplementationArkIds(ko);

      ArkId ark = arks.get(0);

      log.info("ArkId: " + ark.getDashArkImplementation());

      JsonNode implementationMetadata = knowledgeObjectRepository
          .findImplementationMetadata(ark);

      JsonNode deploymentSpecification = knowledgeObjectRepository
          .findDeploymentSpecification(ark, implementationMetadata);

      JsonNode serviceDescription = knowledgeObjectRepository
          .findServiceSpecification(ark, implementationMetadata);

      (serviceDescription.get("paths").fields()).forEachRemaining(endpointEntry -> {

        String endpointPath = endpointEntry.getKey();
        JsonNode endpointSpec = deploymentSpecification.get("endpoints").get(endpointPath);
        String adapterType = endpointSpec.get("adapterType").asText();

        final String payloadPath = implementationMetadata.get(KnowledgeObject.PAYLOAD_TERM).asText();

        byte[] payload = knowledgeObjectRepository.findPayload(ark,
            payloadPath);

        // endpoints.put(arkKey, activate(endpoint, payload, deploymentDescriptor, serviceDescriptor));

      });

      log.info("Endpoints: " + serviceDescription.findValuesAsText("paths"));

    }

    super.loadAndActivateEndPoints();
  }

  private List<ArkId> getImplementationArkIds(JsonNode ko) {
    JsonNode implementations = ko
        .get(KnowledgeObject.IMPLEMENTATIONS_TERM);

    assert(!implementations.isNull());

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

  @Override
  public HashMap<String, EndPoint> getEndpoints() {
    return super.getEndpoints();
  }

  @Override
  EndPoint activateKnowledgeObjectEndpoint(KnowledgeObject knowledgeObject)
      throws AdapterException {
    // Rename as 'activateEndpoints(ArkId, JsonNode)'
    // Accepts an ArkId and JsonNode  speccing the Implementation
    // returns a Map<ArkId, Endpoint> which can be added to the
    // master Map of Endpoints
    return super.activateKnowledgeObjectEndpoint(knowledgeObject);
  }

  @Override
  protected void validateEndPoint(KnowledgeObject knowledgeObject) {

    /* Need a simple way to validate that an object can be activated
     * The ActivatorException thrown in ActivateKnowledgeObjectEndpoint
     * is used to skip objects on the shelf if they aren't valid
     */
    super.validateEndPoint(knowledgeObject);
    log.info(String.format("valid ko @ " + knowledgeObject.getArkId()));
  }

  @Override
  public void startEndpointWatcher() throws IOException {
    super.startEndpointWatcher();
  }
}



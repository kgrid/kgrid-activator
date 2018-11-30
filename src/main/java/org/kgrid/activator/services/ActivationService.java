package org.kgrid.activator.services;

import java.io.IOException;
import java.util.HashMap;
import org.kgrid.activator.EndPoint;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.shelf.domain.KnowledgeObject;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class ActivationService extends ActivationServiceDeprecated {

  @Override
  public HashMap<String, EndPoint> getEndpoints() {
    return super.getEndpoints();
  }

  @Override
  public void loadAndActivateEndPoints() {
    // fetch the Map<ArkId, JsonNode> using findAll()
    // for each ko (== JsonNode) get the Implementations
    // as a collection of ArkIds, JsonNode pairs and then
    // loop through calling activateEndpoints(ArkId, JsonNode) on each Implementation
    // Add each return Map<ArkId, Endpoint> to the mater Map
    super.loadAndActivateEndPoints();
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



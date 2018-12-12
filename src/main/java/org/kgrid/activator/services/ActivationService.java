package org.kgrid.activator.services;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import org.kgrid.activator.EndPointResult;
import org.kgrid.adapter.api.Adapter;
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
  private final AdapterService adapterService;
  private final KnowledgeObjectRepository knowledgeObjectRepository;
  Map<String, Endpoint> endpoints = new HashMap<>();

  public ActivationService(KnowledgeObjectRepository repo, AdapterService adapterService) {
    this.knowledgeObjectRepository = repo;
    this.adapterService = adapterService;
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
    return endpoints;
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
    eps.forEach((key, value) -> {
      Executor executor = activate(key, value);
      value.setExecutor(executor);
    });
  }

  public Executor activate(String endpointKey, Endpoint endpoint) {

    ArkId ark = new ArkId(StringUtils.substringBeforeLast(endpointKey, "/"));

    final JsonNode deploymentSpec = endpoint.getDeployment();

    Adapter adapter = adapterService
        .findAdapter(deploymentSpec.get("adapterType").asText());

    final Path artifact = Paths.get(
        ark.getDashArkImplementation(),
        deploymentSpec.get("artifact").asText()
    );

    final String entry = deploymentSpec.get("entry").asText();

    return adapter.activate(artifact, entry);
  }

  public EndPointResult execute(String endpointPath, Object inputs) {

    Executor executor = endpoints.get(endpointPath).getExecutor();
    final Object output = executor.execute(inputs);

    final EndPointResult endPointResult = new EndPointResult(output);
    endPointResult.getInfo().put("inputs", inputs);

    return endPointResult;
  }
}



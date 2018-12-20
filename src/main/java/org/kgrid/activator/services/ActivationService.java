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
import org.kgrid.activator.ActivatorException;
import org.kgrid.activator.EndPointResult;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.ShelfResourceNotFound;
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

  public final Map<String, Endpoint> endpoints;

  public ActivationService(KnowledgeObjectRepository repo, AdapterService adapterService) {
    this.knowledgeObjectRepository = repo;
    this.adapterService = adapterService;
    endpoints = new HashMap<>();
    this.adapterService.setEndpoints(endpoints);
  }

  public Map<String, Endpoint> loadEndpoints() {

    Map<ArkId, JsonNode> kos = knowledgeObjectRepository.findAll();

    for (Entry<ArkId, JsonNode> ko : kos.entrySet()) {
      List<ArkId> arks = getImplementationArkIds(ko.getValue());
      arks.forEach(this::loadEndpoints);
    }
    return endpoints;
  }

  public Map<String, Endpoint> loadEndpoints(ArkId ark) {
    log.info("ArkId: " + ark.getDashArkImplementation());

    JsonNode resource = null;
    try {
      resource = knowledgeObjectRepository.findImplementationMetadata(ark);
    } catch (ShelfResourceNotFound e) {
      log.warn("Cannot load " + ark.getDashArkImplementation() + ": " + e.getMessage());
      return endpoints;
    }

    JsonNode implementationMetadata = resource;
    JsonNode deploymentSpecification = knowledgeObjectRepository
        .findDeploymentSpecification(ark, implementationMetadata);

    JsonNode serviceDescription = knowledgeObjectRepository
        .findServiceSpecification(ark, implementationMetadata);

    Map<String, Endpoint> eps = new HashMap<>();
    serviceDescription.get("paths").fields().forEachRemaining(service -> {

      JsonNode spec = deploymentSpecification.get("endpoints").get(service.getKey());

      final Endpoint endpoint = new Endpoint();
      endpoint.setDeployment(spec);
      endpoint.setService(serviceDescription);
      endpoint.setImpl(implementationMetadata);
      eps.put(ark.getDashArkImplementation() + service.getKey(), endpoint);
    });
    if (null != eps) {
      endpoints.putAll(eps);
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

  public Map<String, Endpoint> getEndpoints() {
    return endpoints;
  }

  @Deprecated
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
      Executor executor = null;
      try {
        executor = activate(key, value);
      } catch (ActivatorException e) {
        log.warn(e.getMessage());
      }
      value.setExecutor(executor);
    });
  }

  public Executor activate(String endpointKey, Endpoint endpoint) {

    ArkId ark = new ArkId(StringUtils.substringBeforeLast(endpointKey, "/"));

    final JsonNode deploymentSpec = endpoint.getDeployment();

    if (null == deploymentSpec) {
      throw new ActivatorException("No deployment specification for " + endpointKey);
    }

    Adapter adapter = adapterService
        .findAdapter(deploymentSpec.get("adapterType").asText());

    final Path artifact = Paths.get(
        ark.getDashArkImplementation(),
        deploymentSpec.get("artifact").asText()
    );

    final String entry = deploymentSpec.get("entry").asText();

    try {
      return adapter.activate(artifact, entry);
    } catch (AdapterException e) {
      throw new ActivatorException(e.getMessage(), e);
    }
  }

  public EndPointResult execute(String endpointPath, Object inputs) {

    Executor executor = endpoints.get(endpointPath).getExecutor();

    if (null == executor) {
      throw (new ActivatorException("Executor not found for " + endpointPath));
    }

    final Object output = executor.execute(inputs);

    final EndPointResult endPointResult = new EndPointResult(output);

    endPointResult.getInfo().put("inputs", inputs);

    return endPointResult;
  }
}



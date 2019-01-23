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
  private final KnowledgeObjectRepository knowledgeObjectRepository;

  public Map<String, Endpoint> endpoints;

  private AdapterResolver adapterResolver;

  public ActivationService(KnowledgeObjectRepository repo,
      AdapterResolver adapterResolver) {
    this.knowledgeObjectRepository = repo;
    this.adapterResolver = adapterResolver;
    endpoints = new HashMap<>();
  }

  public Map<String, Endpoint> getEndpoints() {
    return endpoints;
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
        log.warn("Could not activate " + key + " " + e.getMessage());
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

    Adapter adapter = adapterResolver
        .getAdapter(deploymentSpec.get("adapterType").asText());

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

    final Endpoint endpoint = endpoints.get(endpointPath);
    Executor executor = endpoint.getExecutor();

    if (null == executor) {
      throw (new ActivatorException("Executor not found for " + endpointPath));
    }

    final Object output = executor.execute(inputs);

    final EndPointResult endPointResult = new EndPointResult(output);

    endPointResult.getInfo().put("inputs", inputs);
    endPointResult.getInfo().put("ko", endpoint.getImpl());

    return endPointResult;
  }


  // TODO: Move to constructor
  public void setEndpoints(Map<String, Endpoint> endpoints) {
    this.endpoints = endpoints;
  }
}



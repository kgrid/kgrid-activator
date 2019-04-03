package org.kgrid.activator.services;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.kgrid.activator.ActivatorException;
import org.kgrid.activator.EndPointResult;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.domain.KnowledgeObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActivationService {

  final Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private Map<String, Endpoint> endpoints;

  @Autowired
  private AdapterResolver adapterResolver;

  public ActivationService(AdapterResolver adapterResolver, Map<String, Endpoint> endpoints) {
    this.adapterResolver = adapterResolver;
    this.endpoints = endpoints;
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

    log.info("Activate endpoint {} ", endpointKey);

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
}



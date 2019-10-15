package org.kgrid.activator.services;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;
import org.kgrid.activator.ActivatorException;
import org.kgrid.activator.EndPointResult;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.domain.KnowledgeObject;
import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActivationService {

  final Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private Map<EndpointId, Endpoint> endpoints;

  @Autowired
  private AdapterResolver adapterResolver;

  @Autowired
  private KnowledgeObjectRepository koRepo;

  public ActivationService(AdapterResolver adapterResolver, Map<EndpointId, Endpoint> endpoints) {
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

  public void activate(Map<EndpointId, Endpoint> eps) {
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

  public Executor activate(EndpointId endpointKey, Endpoint endpoint) {

    log.info("Activate endpoint {} ", endpointKey);

    ArkId ark = endpointKey.getArkId();

    final JsonNode deploymentSpec = endpoint.getDeployment();

    if (null == deploymentSpec) {
      throw new ActivatorException("No deployment specification for " + endpointKey);
    }

    Adapter adapter = adapterResolver
        .getAdapter(deploymentSpec.get("adapterType").asText());

    final Path artifact = Paths.get(
        koRepo.getObjectLocation(ark), ark.getImplementation(),
        deploymentSpec.get("artifact").asText()
    );

    final String entry = deploymentSpec.get("entry").asText();

    try {
      return adapter.activate(artifact, entry);
    } catch (AdapterException e) {
      throw new ActivatorException(e.getMessage(), e);
    }
  }

  public EndPointResult execute (EndpointId id, String version, Object inputs) {
    Endpoint endpoint = endpoints.get(id);

    // How to pick unspecified version?
    if(version == null) {
      for(Entry<EndpointId, Endpoint> entry : endpoints.entrySet() ){
        if(entry.getKey().getArkId().getSlashArk().equals(id.getArkId().getSlashArk())
            && entry.getKey().getEndpointName().equals(id.getEndpointName())) {
          endpoint = entry.getValue();
          break;
        }
      }
    }
    if(null == endpoint) {
      throw new ActivatorException("No endpoint found for " + id);
    }
    Executor executor = endpoint.getExecutor();

    if (null == executor) {
      throw new ActivatorException("No executor found for " + id);
    }

    final Object output = executor.execute(inputs);

    final EndPointResult endPointResult = new EndPointResult(output);

    endPointResult.getInfo().put("inputs", inputs);
    endPointResult.getInfo().put("ko", endpoint.getImpl());

    return endPointResult;
  }

  public EndPointResult execute(EndpointId endpointPath, Object inputs) {

    final Endpoint endpoint = endpoints.get(endpointPath);
    if(null == endpoint) {
      throw new ActivatorException("No endpoint found for " + endpointPath);
    }

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



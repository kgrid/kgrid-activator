package org.kgrid.activator.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.kgrid.activator.ActivatorException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.AdapterSupport;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.domain.KnowledgeObject;
import org.kgrid.shelf.repository.FilesystemCDOStore;
import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ActivationService {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private HashMap<String, Adapter> adapters;
  private long knowledgeObjectsFound;

  @Value("${kgrid.shelf.cdostore.filesystem.location}")
  private String locationStoragePath;

  @Autowired
  KnowledgeObjectRepository knowledgeObjectRepository;

  private HashMap<String, Executor> endpointExecutors = new HashMap<String, Executor>();

  public ActivationService() {
  }


  public void loadAndInitializeAdapters() {

    adapters = new HashMap<>();

    ServiceLoader<Adapter> loader = ServiceLoader.load(Adapter.class);
    for (Adapter adapter : loader) {
      initializeAdapter(adapter);
      adapters.put(adapter.getType().toUpperCase(), adapter);
    }
  }

  protected void initializeAdapter(Adapter adapter) {
    if (adapter instanceof AdapterSupport) {
      ((AdapterSupport) adapter).setCdoStore(new FilesystemCDOStore(locationStoragePath));
    }
    adapter.initialize();
  }


  protected Adapter findAdapter(String adapterType) {
    return adapters.get(adapterType.toUpperCase());
  }

  public HashMap<String, Adapter> getLoadedAdapters() {
    return adapters;
  }

  public HashMap<String, Executor> getEndpointExecutors() {
    return endpointExecutors;
  }

  public long getKnowledgeObjectsFound() {
    return knowledgeObjectsFound;
  }

  public HashMap<String, Executor> getLoadedExecutors() {
    return endpointExecutors;
  }

  /**
   * Gets all of the knowledge object versions and activates the endpoint for each.  A map of
   * endpoint executors is created.
   */
  public void loadAndActivateEndpoints() {

    //TODO All too hard
    //Load all of the ko including all versions
    Map<ArkId, Map<String, ObjectNode>> koList = knowledgeObjectRepository.findAll();

    for (Entry<ArkId, Map<String, ObjectNode>> ko : koList.entrySet()) {
      knowledgeObjectsFound++;

      for (Entry<String, ObjectNode> version : ko.getValue().entrySet()) {

        KnowledgeObject knowledgeObject = knowledgeObjectRepository
            .findByArkIdAndVersion(ko.getKey(), version.getKey());

        try {

          endpointExecutors.put(this.getExecutorKey(knowledgeObject),
              activateKnowledgeObjectEndPoint(knowledgeObject));

        } catch (ActivatorException e) {
          log.error("Couldn't activate KnowledgeObject EndPoint" + e.getMessage());
        }

      }

    }

  }

  public KnowledgeObject getKnowledgeObject(ArkId arkId, String version) {
    return knowledgeObjectRepository.findByArkIdAndVersion(arkId, version);
  }

  //TODO  Need to fix the ark id so getting naan and name is posiible, we now have ark:/ or naan-name options
  public String getExecutorKey(KnowledgeObject knowledgeObject) {
    return knowledgeObject.getArkId().getFedoraPath().replace('-','/') +  "/" + knowledgeObject.version() +
        "/" + knowledgeObject.getModelMetadata().get("functionName").asText();
  }


  /**
   * Will find the applicable adapter for a Knowledge Object and activate the endpoint
   *
   * @return Executor
   */
  Executor activateKnowledgeObjectEndPoint(KnowledgeObject knowledgeObject)
      throws AdapterException {

    Path modelPath = knowledgeObject.getModelDir();
    JsonNode endPointMetadata = knowledgeObject.getModelMetadata();

    Adapter adapter = adapters.get(endPointMetadata.get("adapterType").asText().toUpperCase());

    if (adapter == null) {

      String message =
          "No " + endPointMetadata.get("adapterType") + "adapter type found for ko "
              + knowledgeObject
              .getArkId() + ":" + knowledgeObject
              .version();

      log.error(message);

      throw new ActivatorException(message);
    }

    //TODO  Assuming function name will be used as endpoint will change :-)
    String functionName = endPointMetadata.get("functionName").asText();

    return adapter.activate(modelPath.resolve("resource"), functionName);

  }

}



package org.kgrid.activator.services;

import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.umich.lhs.activator.exception.ActivatorException;
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

  @Value("${shelf.location}")
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

  /**
   * Gets all of the knowledge object versions and activates the endpoint for each.  A map of
   * endpoint executors is created.
   */
  public void activateKnowledgeObjects() {

    //TODO All too hard
    //Load all of the ko including all versions
    Map<String, Map<String, ObjectNode>> koList = knowledgeObjectRepository.findAll();

    for (Entry<String, Map<String, ObjectNode>> entry : koList.entrySet()) {

      for (Entry<String, ObjectNode> version : entry.getValue().entrySet()) {

        KnowledgeObject knowledgeObject = knowledgeObjectRepository
            .findByArkIdAndVersion(new ArkId(entry.getKey()), version.getKey());

        try {

          endpointExecutors.put(knowledgeObject.getArkId() + knowledgeObject.version() +
                  ":" + knowledgeObject.getFunctionName(),
                          activateKnowledageObjectEndPoint(knowledgeObject));

        } catch (ActivatorException e) {
          log.error("Couldn't activate KnowledageObject EndPoint" + e.getMessage());
        }

      }

    }

  }

  /**
   * Will find the applicable adapter for a Knowledge Object and activate the endpoint
   *
   * @param knowledgeObject
   * @return Executer
   * @throws AdapterException
   */
  public Executor activateKnowledageObjectEndPoint(KnowledgeObject knowledgeObject)
      throws AdapterException {

    Adapter adapter;

    if (adapters.get(knowledgeObject.adapterType()) == null) {

      String message =
          "No " + knowledgeObject.adapterType() + "adapter type found for ko " + knowledgeObject
              .getArkId() + ":" + knowledgeObject
              .version();

      log.error(message);

      throw new ActivatorException(message);

    } else {
      adapter = adapters.get(knowledgeObject.adapterType());
    }

    //TODO  Assuming function name will be used as endpoint will change :-)
    Path resourcePath = knowledgeObject.getResourceLocation();
    String functionName = knowledgeObject.getFunctionName();

    return adapter.activate(resourcePath, functionName);

  }
}



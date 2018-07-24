package org.kgrid.activator.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.file.Paths;
import java.util.Objects;
import org.kgrid.activator.ActivatorException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import org.kgrid.activator.EndPoint;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.AdapterSupport;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.domain.KnowledgeObject;
import org.kgrid.shelf.repository.CompoundDigitalObjectStore;
import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActivationService {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private HashMap<String, Adapter> adapters;
  private long knowledgeObjectsFound;
  @Autowired
  private ServiceDescriptionService serviceDescriptionService;

  @Autowired
  CompoundDigitalObjectStore cdoStore;

  @Autowired
  KnowledgeObjectRepository knowledgeObjectRepository;

  private HashMap<String, EndPoint> endpoints = new HashMap<String, EndPoint>();

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
      ((AdapterSupport) adapter).setCdoStore(cdoStore);
    }
    adapter.initialize();
  }


  protected Adapter findAdapter(String adapterType) {
    return adapters.get(adapterType.toUpperCase());
  }

  public HashMap<String, Adapter> getLoadedAdapters() {
    return adapters;
  }

  public HashMap<String, EndPoint> getEndpoints() { return endpoints; }

  public long getKnowledgeObjectsFound() {
    return knowledgeObjectsFound;
  }


  /**
   * Gets all of the knowledge object versions and activates the endpoint for each.  A map of
   * endpoint executors is created.
   */
  public void loadAndActivateEndPoints() {

    //TODO All too hard
    //Load all of the ko including all versions

    endpoints.clear();

    Map<ArkId, Map<String, ObjectNode>> koList = knowledgeObjectRepository.findAll();

    for (Entry<ArkId, Map<String, ObjectNode>> ko : koList.entrySet()) {
      knowledgeObjectsFound++;

      for (Entry<String, ObjectNode> version : ko.getValue().entrySet()) {

        KnowledgeObject knowledgeObject = knowledgeObjectRepository
            .findByArkIdAndVersion(ko.getKey(), version.getKey());

        EndPoint endPoint =null;

        try {

          endPoint = activateKnowledgeObjectEndpoint(knowledgeObject);

          endpoints.put(endPoint.getEndPointAbsolutePath(), endPoint);

        } catch (ActivatorException activatorException) {
          log.warn("Activator couldn't activate KnowledgeObject EndPoint " +
              ko.getKey() + "/" + version.getKey()+ " - " + activatorException.getMessage());
        } catch (AdapterException adapterException ){
          log.warn("Adapter couldn't activate KnowledgeObject EndPoint " +
              ko.getKey() + "/" + version.getKey()+ " - " + adapterException.getMessage());
        }

      }

    }

  }
  public String getKnowleledgeObjectPath(KnowledgeObject knowledgeObject) {

    return knowledgeObject.getArkId().getFedoraPath().replace('-', '/') + "/" + knowledgeObject
        .version();
  }

  public String getEndPointPath(KnowledgeObject knowledgeObject){

    return serviceDescriptionService.findPath(knowledgeObject);
  }


  /**
   *
   * Will find the applicable adapter for a Knowledge Object and activate the endpoint
   *
   * @return EndPoint
   */
  EndPoint activateKnowledgeObjectEndpoint(KnowledgeObject knowledgeObject)
      throws AdapterException {

    Path modelPath = knowledgeObject.getModelDir();

    validateEndPoint(knowledgeObject);

    JsonNode modelMetadata = knowledgeObject.getModelMetadata();

   if (adapters.containsKey(modelMetadata.get("adapterType").asText().toUpperCase())){

     Adapter adapter = adapters.get(modelMetadata.get("adapterType").asText().toUpperCase());

     String functionName = modelMetadata.get("functionName").asText();

     Path resource = modelPath.resolve(modelMetadata.get("resource").asText());

     Executor executor = adapter.activate(resource, functionName);

     //TODO  Stupid
     String endPointPath = getEndPointPath(knowledgeObject);

     String baseKOPath = getKnowleledgeObjectPath(knowledgeObject);

     EndPoint endPoint =  new EndPoint(executor, endPointPath, baseKOPath);

     return endPoint;

   } else {

     throw new ActivatorException( modelMetadata.get("adapterType") + " adapter type found");

   }

  }

  /**
   * Validates that there is enough information to create an EndPoint
   * @param knowledgeObject
   */
  protected void validateEndPoint(KnowledgeObject knowledgeObject) {

    JsonNode endPointMetadata = knowledgeObject.getModelMetadata();

    try {
      Objects.requireNonNull(
          endPointMetadata.get("adapterType"), "Adapter Type on Model Required");
      Objects.requireNonNull(
          endPointMetadata.get("functionName"), "Function Name on Model Required");
      Objects.requireNonNull(
          endPointMetadata.get("resource"), "Resource on Model Required");
      Objects.requireNonNull(
          serviceDescriptionService.loadServiceDescription(knowledgeObject), "Service Description is Required");
      Objects.requireNonNull(
          serviceDescriptionService.findPath(knowledgeObject),"Service Description Paths are Required");
    } catch (NullPointerException exception){
      throw new ActivatorException(exception.getMessage());
    }


  }

}



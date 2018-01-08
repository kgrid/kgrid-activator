package edu.umich.lhs.activator.domain;

import static edu.umich.lhs.activator.services.KobjectImporter.KNOWLEDGE_OBJECT_URI;
import static edu.umich.lhs.activator.services.KobjectImporter.identifierProp;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Created by grosscol on 2018-01-04.
 */
public class KobjectBuilder {
  private Kobject ko;

  public KobjectBuilder() {
    ko = new Kobject();
  }

  public KobjectBuilder metadata(Metadata metadata) {
    ko.metadata = metadata;
    return this;
  }

  public KobjectBuilder noofParams(Integer i) {
    ko.setNoofParams(i);
    return this;
  }

  public KobjectBuilder addParamDescription(String name, DataType dataType, Integer min, Integer max){
    ParamDescription p = new ParamDescription(name, dataType, min, max);
    ko.addParamDescriptions(p);
    return this;
  }

  public KobjectBuilder returnType(Class c) {
    ko.setReturnType(c);
    return this;
  }

  public KobjectBuilder payload(Payload payload) {
    ko.setPayload(payload);
    return this;
  }

  public KobjectBuilder url(String url) {
    ko.setUrl(url);
    return this;
  }

  public KobjectBuilder payloadContent (String content) {
    if(ko.getPayload() != null) {
      ko.getPayload().setContent(content);
    } else {
      ko.setPayload(new Payload(content, "",""));
    }
    return this;
  }

  public KobjectBuilder payloadFunctionName (String functionName) {
    if(ko.getPayload() != null) {
      ko.getPayload().setFunctionName(functionName);
    } else {
      ko.setPayload(new Payload("","",functionName));
    }
    return this;
  }

  public KobjectBuilder payloadEngineType (String engineType) {
    if(ko.getPayload() != null) {
      ko.getPayload().setEngineType(engineType);
    } else {
      ko.setPayload(new Payload("",engineType, ""));
    }
    return this;
  }

  public KobjectBuilder arkID(ArkId ark){
    ko.setIdentifier(ark);
    return this;
  }

  // Build a minimal RDF Model to allow saving something with an ark
  private Model buildModel(Kobject ko){
    String identifier;
    if(ko.getIdentifier() == null){
      identifier = new ArkId("foo","bar").toString();
    }
    else{
      identifier = ko.getIdentifier().toString();
    }

    Resource kobjectType = ResourceFactory.createResource(KNOWLEDGE_OBJECT_URI);
    Model model = ModelFactory.createDefaultModel();
    model.createResource("http://example.com/1", kobjectType)
        .addProperty(identifierProp, identifier);

    return model;
  }

  public Kobject build(){
    ko.setRdfModel(buildModel(ko));
    return ko;
  }



}
package edu.umich.lhs.activator.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Map;
import org.apache.jena.rdf.model.Model;

/**
 * Created by grosscol on 2017-09-11.
 */
public class Kobject implements PayloadProvider {

  public Metadata metadata;

  private Payload payload;
  private Integer noofParams;
  private ArrayList<ParamDescription> paramDescriptions;
  private Class returnType;
  private ArkId identifier;
  private String url;


  @JsonIgnore
  private Model rdfModel;

  public Kobject(){
    paramDescriptions = new ArrayList<>();
    payload = new Payload();
    url = "";
    returnType = Map.class;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  @Override
  public String toString() {
    return "Kobject [FunctionName=" + payload.getFunctionName() +
        ", engineType=" + payload.getEngineType() +
        ", payloadContent=" + payload.getContent() +
        ", url=" + url +
        "]";
  }

  @Override
  public Class getReturnType() {
    return returnType;
  }

  public void setReturnType(Class c) {
    returnType = c;
  }

  @Override
  public int getNoOfParams() {
    if(noofParams == null){
      return 0;
    }
    return noofParams;
  }

  @Override
  public ArrayList<ParamDescription> getParams() {
    return paramDescriptions;
  }

  @Override
  public String getFunctionName() {
    return payload.getFunctionName();
  }

  @Override
  public String getContent() {
    return payload.getContent();
  }

  @Override
  public String getEngineType() {
    return payload.getEngineType();
  }

  //TODO: Refactor such that this is not required?  Use payload provider or suitable interface?
  public Payload getPayload() {
    return payload;
  }

  public ArkId getIdentifier() {
    return identifier;
  }

  public void setIdentifier(ArkId id) {
    identifier = id;
  }

  public void setParamDescriptions(ArrayList<ParamDescription> p) {
    paramDescriptions = p;
  }

  public void addParamDescriptions(ParamDescription p) {
    paramDescriptions.add(p);
  }

  public void setNoofParams(Integer i) {
    noofParams = i;
  }

  public void setPayload(Payload p) {
    payload = p;
  }

  public Model getRdfModel() {
    return rdfModel;
  }

  public void setRdfModel(Model rdfModel) {
    this.rdfModel = rdfModel;
  }
}

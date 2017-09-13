package edu.umich.lhs.activator.domain;

import edu.umich.lhs.activator.domain.Payload;
import java.util.ArrayList;
import java.util.Map;

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

  @Override
  public Class getReturnType() {
    return Map.class;
  }

  @Override
  public int getNoOfParams() {
    return 0;
  }

  @Override
  public ArrayList<ParamDescription> getParams() {
    return new ArrayList<>();
  }

  @Override
  public String getFunctionName() {
    return "";
  }

  @Override
  public String getContent() {
    return "";
  }

  @Override
  public String getEngineType() {
    return "";
  }

  public ArkId getIdentifier(){
    return identifier;
  }

  public void setIdentifier(ArkId id){
    identifier = id;
  }

  public void setParamDescriptions(ArrayList<ParamDescription> p){
    paramDescriptions = p;
  }

  public void setNoofParams(Integer i){
    noofParams = i;
  }

  public void setReturnType(Class c){
    returnType = c;
  }

  public void setPayload(Payload p){
    payload = p;
  }




}

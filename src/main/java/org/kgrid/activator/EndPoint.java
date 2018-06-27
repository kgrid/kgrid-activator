package org.kgrid.activator;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.domain.ArkId;

/**
 * Defines an activated knowledge object endpoint
 *
 * */
public class EndPoint {

  private Executor executor;
  private ArkId arkId;
  private String version;
  private String path;


  public EndPoint(ArkId arkId, String version , Executor executor, String path) {
    this.arkId= arkId;
    this.executor = executor;
    this.version = version;
    this.path = path;
  }
  public Object executeEndPoint(Object input){
    return executor.execute( input );
  }
  public Executor getExecutor() { return executor; }

  public ArkId getArkId() {
    return arkId;
  }

  public String getPath() {
    return path;
  }

  public String getVersion() {
    return version;
  }

  public String getEndPointPath() {
    return arkId.getFedoraPath().replace('-', '/') + "/" + version + path;


  }
}

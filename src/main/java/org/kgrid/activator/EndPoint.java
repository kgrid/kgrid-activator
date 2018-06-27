package org.kgrid.activator;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.kgrid.adapter.api.Executor;

/**
 * Defines an activated knowledge object endpoint
 *
 * */
public class EndPoint {

  private Executor executor;
  private String endPointPath;
  private JsonNode serviceDescription;

  public EndPoint(String endPointPath, Executor executor, JsonNode serviceDescription) {
    this.endPointPath= endPointPath;
    this.executor = executor;
    this.serviceDescription = serviceDescription;
  }
  public Object executeEndPoint(Object input){
    return executor.execute( input );
  }
  public String getEndPointPath() {
    return endPointPath;
  }
  public Executor getExecutor() { return executor; }
  public JsonNode getServiceDescription() { return serviceDescription; }

  @Override
  public String toString() {
    return new ToStringBuilder(this).
        append("endPointPath", endPointPath).
        toString();
  }
}

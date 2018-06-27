package org.kgrid.activator;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.kgrid.adapter.api.Executor;

/**
 * Defines an activated knowledge object endpoint
 *
 * */
public class EndPoint {

  private Executor executor;
  private String endPointPath;


  public EndPoint(String endPointPath, Executor executor) {
    this.endPointPath= endPointPath;
    this.executor = executor;
  }
  public Object executeEndPoint(Object input){
    return executor.execute( input );
  }
  public String getEndPointPath() {
    return endPointPath;
  }
  public Executor getExecutor() {
    return executor;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).
        append("endPointPath", endPointPath).
        toString();
  }
}

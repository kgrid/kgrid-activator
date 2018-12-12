package org.kgrid.activator;

import org.kgrid.adapter.api.Executor;

/**
 * Defines an activated knowledge object endpoint
 *
 * */
@Deprecated
public class EndPoint {

  private Executor executor;
  private String knowledgeObjectEndPointPath;
  private String endPointPath;


  public EndPoint( Executor executor, String endPointPath, String knowledgeObjectEndPointPath) {
    this.endPointPath= endPointPath;
    this.executor = executor;
    this.knowledgeObjectEndPointPath = knowledgeObjectEndPointPath;

  }
  public Object executeEndPoint(Object input){
    return executor.execute( input );
  }
  public Executor getExecutor() { return executor; }

  public String getKnowledgeObjectEndPointPath() {
    return knowledgeObjectEndPointPath;
  }

  public String getEndPointPath() {
    return endPointPath;
  }

  public String getEndPointAbsolutePath() {
    return getKnowledgeObjectEndPointPath() + getEndPointPath();
  }
}

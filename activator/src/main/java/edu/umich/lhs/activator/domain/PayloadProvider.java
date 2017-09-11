package edu.umich.lhs.activator.domain;

import java.util.ArrayList;

/**
 * Created by grosscol on 2017-09-11.
 */
public interface PayloadProvider {
  public int getNoOfParams();
  public ArrayList<ParamDescription> getParams();
  public String getFunctionName();
  public String getContent();
  public String getEngineType();
  public Class getReturnType();
}

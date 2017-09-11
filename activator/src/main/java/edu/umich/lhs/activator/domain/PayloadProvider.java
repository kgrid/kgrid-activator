package edu.umich.lhs.activator.domain;

import java.util.ArrayList;

/**
 * Created by grosscol on 2017-09-11.
 */
public interface PayloadProvider {
  public Class getReturnType();
  public int getNoOfParams();
  public ArrayList<ParamDescription> getParams();
}

package edu.umich.lhs.activator.domain;

import java.util.ArrayList;

/**
 * Created by grosscol on 2017-09-11.
 */
public interface PayloadProvider {

  /**
   * @return number of parameters of payload function
   */
  public int getNoOfParams();

  /**
   * @return array of descriptions of parameters
   */
  public ArrayList<ParamDescription> getParams();

  /**
   * @return name of the function in the payload for the activator to call
   */
  public String getFunctionName();

  /**
   * @return content of the payload for the activator to execute or serve
   */
  public String getContent();

  /**
   * @return name of the engine for the activator to match with an appropriate adapter
   */
  public String getEngineType();

  /**
   * @return Java class that the result of the payload should be cast to.
   */
  public Class getReturnType();
}

package edu.umich.lhs.activator.domain;

import edu.umich.lhs.activator.domain.Payload;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by grosscol on 2017-09-11.
 */
public class Kobject implements PayloadProvider {

  public Metadata metadata;
  public Payload payload;

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


}

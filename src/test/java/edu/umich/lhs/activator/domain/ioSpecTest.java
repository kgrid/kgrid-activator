package edu.umich.lhs.activator.domain;

import static org.junit.Assert.*;

import edu.umich.lhs.activator.services.ioSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by nggittle on 3/23/2017.
 */
public class ioSpecTest {

  private edu.umich.lhs.activator.services.ioSpec ioSpec;
  ArrayList<ParamDescription> paramList;

  @Before
  public void setUp() throws Exception {
    paramList = new ArrayList<>();
    ioSpec = new ioSpec();
  }

  @Test
  public void givenAMissingParamReportsCorrectly() throws Exception {
    paramList.add(new ParamDescription("rxcui2", DataType.STRING, 0, 2));
    ioSpec.setParams(paramList);
    ioSpec.setNoOfParams(paramList.size());

    Map<String, Object> inputs = new HashMap<>();
    inputs.put("rxcui", "1723222 2101 10767");
    assertEquals(" Input parameter rxcui2 is missing.", ioSpec.verifyInput(inputs));
  }

  @Test
  public void givenTwoMissingParamsContainsBoth() throws Exception {
    paramList.add(new ParamDescription("rxcui2", DataType.STRING, 0, 2));
    paramList.add(new ParamDescription("name", DataType.STRING, 0, 2));
    ioSpec.setParams(paramList);
    ioSpec.setNoOfParams(paramList.size());

    Map<String, Object> inputs = new HashMap<>();
    inputs.put("rxcui", "1723222 2101 10767");
    inputs.put("rxcu", "1723222 2101 10767");
    assertEquals(" Input parameter rxcui2 is missing. Input parameter name is missing.", ioSpec.verifyInput(inputs));

  }

}
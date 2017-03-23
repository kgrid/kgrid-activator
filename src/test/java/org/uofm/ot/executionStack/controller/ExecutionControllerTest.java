package org.uofm.ot.executionStack.controller;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.uofm.ot.executionStack.ObjectTellerExecutionStackApplication;
import org.uofm.ot.executionStack.exception.OTExecutionStackException;
import org.uofm.ot.executionStack.transferObjects.KnowledgeObjectDTO;
import org.uofm.ot.executionStack.transferObjects.Result;

/**
 * Created by nggittle on 3/22/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ObjectTellerExecutionStackApplication.class})
public class ExecutionControllerTest {

  public static final String INPUT_SPEC
      = "<rdf:RDF xmlns:ot='http://uofm.org/objectteller/' xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'>"
      + "<rdf:Description rdf:about='http://uofm.org/objectteller/inputMessage'>"
      + "<ot:noofparams>1</ot:noofparams>"
      + "<ot:params>"
      + "<rdf:Seq>"
      + "<rdf:li>rxcui</rdf:li>"
      + "</rdf:Seq>"
      + "</ot:params>"
      + "</rdf:Description>"
      + "<rdf:Description rdf:about='http://uofm.org/objectteller/rxcui/'>"
      + "<ot:datatype>MAP</ot:datatype>"
      + "</rdf:Description>"
      + "</rdf:RDF>";

  private static final String OUTPUT_SPEC
      = "<rdf:RDF xmlns:ot='http://uofm.org/objectteller/' xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'>"
      + "<rdf:Description rdf:about='http://uofm.org/objectteller/outputMessage'>"
      + "<ot:returntype>STRING</ot:returntype> </rdf:Description> </rdf:RDF>";

  @Autowired
  private ExecutionController ex;

  @Test(expected = NullPointerException.class)
  public void testCalculateWithNullInputsAndNullKO() throws Exception {
    ex.validateAndExecute(null, null);
  }

  @Test(expected = OTExecutionStackException.class)
  public void testCalculateWithKOandNullInputs() throws Exception {

    KnowledgeObjectDTO ko = new KnowledgeObjectDTO();
    assertNotNull(ex.validateAndExecute(null, ko));
  }

  @Test(expected = OTExecutionStackException.class)
  public void testCalculateWithBadKOandInputs() throws Exception {
    KnowledgeObjectDTO ko = new KnowledgeObjectDTO();
    Map<String, Object> inputs = new HashMap<>();
    inputs.put("test", "test");
    ko.inputMessage = INPUT_SPEC;
    ko.outputMessage = OUTPUT_SPEC;
    assertNotNull(ex.validateAndExecute(inputs, ko));
  }

  @Test(expected = OTExecutionStackException.class)
  public void testCalculateWithExpectedBadOutput() {
    KnowledgeObjectDTO ko = new KnowledgeObjectDTO();
    Map<String, Object> inputs = new HashMap<>();
    inputs.put("test", "test");
    Result expectedResult = new Result(null);
    expectedResult.setSource(null);
    ko.inputMessage = INPUT_SPEC;
    ko.outputMessage = OUTPUT_SPEC;
    Result generatedResult = ex.validateAndExecute(inputs, ko);
    assertEquals(expectedResult, generatedResult);
  }

  @Test
  public void testCalculateWithExpectedOkOutput() {
    KnowledgeObjectDTO ko = new KnowledgeObjectDTO();
    Map<String, Object> inputs = new HashMap<>();
    inputs.put("rxcui", "1723222 2101 10767");
    Result expectedResult = new Result(null);
    expectedResult.setSource(null);
    ko.inputMessage = INPUT_SPEC;
    ko.outputMessage = OUTPUT_SPEC;
    ko.genPayload("def execute():return true", "PYTHON", "execute");
    Result generatedResult = ex.validateAndExecute(inputs, ko);
    assertEquals(expectedResult, generatedResult);
  }

}
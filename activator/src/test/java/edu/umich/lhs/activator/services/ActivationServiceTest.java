package edu.umich.lhs.activator.services;

import edu.umich.lhs.activator.TestUtils;
import edu.umich.lhs.activator.domain.DataType;
import edu.umich.lhs.activator.domain.KnowledgeObject;
import edu.umich.lhs.activator.domain.KnowledgeObjectBuilder;
import edu.umich.lhs.activator.domain.Kobject;
import edu.umich.lhs.activator.domain.KobjectBuilder;
import edu.umich.lhs.activator.domain.Result;
import edu.umich.lhs.activator.exception.ActivatorException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

/**
 * Created by nggittle on 3/22/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ActivationServiceTest {

  public static final String payload_code = "function execute(a){ return a.toString()}";

  @Autowired
  private ActivationService activationService;

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Test
  public void testCalculateWithNullInputsAndNullKO() throws Exception {
    expectedEx.expect(NullPointerException.class);
    activationService.validateAndExecute(null, null);
    assertNotNull(null);
  }

  @Test
  public void testCalculateWithEmptyKOandNullInputs() throws Exception {

    Kobject ko = new Kobject();
    expectedEx.expect(ActivatorException.class);
    expectedEx.expectMessage("No inputs given.");
    assertNotNull(activationService.validateAndExecute(null, ko));
  }

  //TODO: Move this spec to a test of the Validator
  @Test
  public void testCalculateWithWrongInput() throws Exception {
    Kobject ko = new KobjectBuilder()
        .addParamDescription("rxcui", DataType.STRING, 1, 1)
        .returnType(String.class)
        .payloadEngineType("JAVASCRIPT")
        .payloadFunctionName("execute")
        .payloadContent(payload_code)
        .build();
    Map<String, Object> inputs = new HashMap<>();
    inputs.put("test", "test");
    expectedEx.expect(ActivatorException.class);
    expectedEx.expectMessage("Error in converting RDF ioSpec for ko:  Input parameter rxcui is missing.");
    assertNotNull(activationService.validateAndExecute(inputs, ko));
  }

  @Test
  public void testCalculateWithCorrectInputsButNoPayload() {
    Kobject ko = new KobjectBuilder()
        .addParamDescription("rxcui", DataType.STRING, 1, 1)
        .returnType(String.class)
        .payloadEngineType("JAVASCRIPT")
        .payloadFunctionName("execute")
        .payloadContent(payload_code)
        .build();
    Map<String, Object> inputs = new HashMap<>();
    inputs.put("rxcui", "test");
    Result expectedResult = new Result();
    expectedResult.setSource(null);

    expectedEx.expect(ActivatorException.class);
    expectedEx.expectMessage("Knowledge object payload content is NULL or empty");
    Result generatedResult = activationService.validateAndExecute(inputs, ko);

  }

  @Test
  public void testCalculateWithSyntaxErrorToThrowEx() {
    Kobject ko = new KobjectBuilder()
        .addParamDescription("rxcui", DataType.INT, 1, 1)
        .returnType(String.class)
        .payloadEngineType("JAVASCRIPT")
        .payloadFunctionName("execute")
        .payloadContent(payload_code)
        .build();

    Map<String, Object> inputs = new HashMap<>();
    inputs.put("rxcui", "1723222 2101 10767");
    Result expectedResult = new Result();
    expectedResult.setSource(null);

    expectedEx.expect(ActivatorException.class);
    Result generatedResult = activationService.validateAndExecute(inputs, ko);
  }

  //TODO: Ensure validator is called

  //TODO: Move this spec to a test of the Validator
  @Test
  public void testCalculateWithTooManyInputsToThrowEx() {
    Kobject ko = new KobjectBuilder()
        .addParamDescription("rxcui", DataType.INT, 1, 1)
        .returnType(String.class)
        .payloadEngineType("JAVASCRIPT")
        .payloadFunctionName("execute")
        .payloadContent(payload_code)
        .build();

    Map<String, Object> inputs = new HashMap<>();
    inputs.put("rxcui", "1723222 2101 10767");
    inputs.put("rxcui2", "1723222 2101 10767");
    Result expectedResult = new Result();
    expectedResult.setSource(null);

    expectedEx.expect(ActivatorException.class);
    expectedEx.expectMessage("Error in converting RDF ioSpec for ko: Number of input parameters should be 1.");
    Result generatedResult = activationService.validateAndExecute(inputs, ko);
  }

  //TODO: Move this spec to a test of the Validator
  @Test
  public void testCalculateWithTooFewInputsToThrowEx() {
    Kobject ko = new KobjectBuilder()
        .addParamDescription("rxcui", DataType.INT, 1, 1)
        .addParamDescription("rxcui2", DataType.INT, 1, 1)
        .returnType(String.class)
        .payloadEngineType("JAVASCRIPT")
        .payloadFunctionName("execute")
        .payloadContent(payload_code)
        .build();

    Map<String, Object> inputs = new HashMap<>();
    inputs.put("rxcui", "1723222 2101 10767");
    Result expectedResult = new Result();
    expectedResult.setSource(null);

    expectedEx.expect(ActivatorException.class);
    expectedEx.expectMessage("Error in converting RDF ioSpec for ko: Number of input parameters should be 2. Input parameter rxcui2 is missing.");
    Result generatedResult = activationService.validateAndExecute(inputs, ko);
  }

  //TODO: Move this spec to a test of the Validator
  @Test
  public void testStringReturnedWhenExpectIntToThrowEx() {
    Kobject ko = new KobjectBuilder()
        .addParamDescription("rxcui", DataType.INT, 1, 1)
        .addParamDescription("rxcui2", DataType.INT, 1, 1)
        .returnType(Integer.class)
        .payloadEngineType("JAVASCRIPT")
        .payloadFunctionName("execute")
        .payloadContent(payload_code)
        .build();
    Map<String, Object> inputs = new HashMap<>();
    inputs.put("rxcui", "1723222");
    Result expectedResult = new Result();
    expectedResult.setResult("{u'rxcui': u'1723222'}");

    expectedEx.expect(ActivatorException.class);
//    expectedEx.expectMessage(contains("Type mismatch while converting javascript result to java"));

    Result generatedResult = activationService.validateAndExecute(inputs, ko);
  }
}
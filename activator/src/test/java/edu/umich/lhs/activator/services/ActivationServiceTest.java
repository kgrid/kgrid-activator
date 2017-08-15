package edu.umich.lhs.activator.services;

import static org.junit.Assert.*;
import static org.mockito.Matchers.contains;

import java.util.HashMap;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import edu.umich.lhs.activator.KgridActivatorApplication;
import edu.umich.lhs.activator.TestUtils;
import edu.umich.lhs.activator.domain.KnowledgeObject;
import edu.umich.lhs.activator.exception.ActivatorException;
import edu.umich.lhs.activator.domain.KnowledgeObjectBuilder;
import edu.umich.lhs.activator.domain.Result;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Created by nggittle on 3/22/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ActivationServiceTest {

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

    KnowledgeObject ko = new KnowledgeObject();
    expectedEx.expect(ActivatorException.class);
    expectedEx.expectMessage("No inputs given.");
    assertNotNull(activationService.validateAndExecute(null, ko));
  }

  @Test
  public void testCalculateWithWrongInput() throws Exception {
    KnowledgeObject ko = new KnowledgeObjectBuilder()
        .inputMessage(TestUtils.INPUT_SPEC_ONE_INPUT)
        .outputMessage(TestUtils.OUTPUT_SPEC_RET_STR)
        .build();
    Map<String, Object> inputs = new HashMap<>();
    inputs.put("test", "test");
    expectedEx.expect(ActivatorException.class);
    expectedEx.expectMessage("Error in converting RDF ioSpec for ko:  Input parameter rxcui is missing.");
    assertNotNull(activationService.validateAndExecute(inputs, ko));
  }

  @Test
  public void testCalculateWithCorrectInputsButNoPayload() {
    KnowledgeObject ko = new KnowledgeObjectBuilder()
        .inputMessage(TestUtils.INPUT_SPEC_ONE_INPUT)
        .outputMessage(TestUtils.OUTPUT_SPEC_RET_STR)
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
    KnowledgeObject ko = new KnowledgeObjectBuilder()
        .inputMessage(TestUtils.INPUT_SPEC_ONE_INPUT)
        .outputMessage(TestUtils.OUTPUT_SPEC_RET_STR)
        .payloadContent("function execute(a) return a}") // Syntax error
        .payloadEngineType("JAVASCRIPT")
        .payloadFunctionName("execute")
        .build();

    Map<String, Object> inputs = new HashMap<>();
    inputs.put("rxcui", "1723222 2101 10767");
    Result expectedResult = new Result();
    expectedResult.setSource(null);

    expectedEx.expect(ActivatorException.class);
    expectedEx.expectMessage(contains("Error occurred while executing javascript code SyntaxError:"));
    Result generatedResult = activationService.validateAndExecute(inputs, ko);
  }

  @Test
  public void testCalculateWithTooManyInputsToThrowEx() {
    KnowledgeObject ko = new KnowledgeObjectBuilder()
        .inputMessage(TestUtils.INPUT_SPEC_ONE_INPUT)
        .outputMessage(TestUtils.OUTPUT_SPEC_RET_STR)
        .payloadContent(TestUtils.CODE)
        .payloadEngineType("JAVASCRIPT")
        .payloadFunctionName("execute")
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

  @Test
  public void testCalculateWithTooFewInputsToThrowEx() {
    KnowledgeObject ko = new KnowledgeObjectBuilder()
        .inputMessage(TestUtils.INPUT_SPEC_TWO_INPUTS)
        .outputMessage(TestUtils.OUTPUT_SPEC_RET_STR)
        .payloadContent(TestUtils.CODE)
        .payloadEngineType("JAVASCRIPT")
        .payloadFunctionName("execute")
        .build();

    Map<String, Object> inputs = new HashMap<>();
    inputs.put("rxcui", "1723222 2101 10767");
    Result expectedResult = new Result();
    expectedResult.setSource(null);

    expectedEx.expect(ActivatorException.class);
    expectedEx.expectMessage("Error in converting RDF ioSpec for ko: Number of input parameters should be 2. Input parameter rxcui2 is missing.");
    Result generatedResult = activationService.validateAndExecute(inputs, ko);
  }

  @Test
  public void testStringReturnedWhenExpectIntToThrowEx() {
    KnowledgeObject ko = new KnowledgeObjectBuilder()
        .inputMessage(TestUtils.INPUT_SPEC_ONE_INPUT)
        .outputMessage(TestUtils.OUTPUT_SPEC_RET_INT)
        .payloadContent(TestUtils.CODE)
        .payloadEngineType("JAVASCRIPT")
        .payloadFunctionName("execute")
        .build();
    Map<String, Object> inputs = new HashMap<>();
    inputs.put("rxcui", "1723222");
    Result expectedResult = new Result();
    expectedResult.setResult("{u'rxcui': u'1723222'}");

    expectedEx.expect(ActivatorException.class);
    expectedEx.expectMessage(contains("Type mismatch while converting javascript result to java"));

    Result generatedResult = activationService.validateAndExecute(inputs, ko);
  }
}
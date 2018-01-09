package edu.umich.lhs.activator.services;

import edu.umich.lhs.activator.TestUtils;
import edu.umich.lhs.activator.domain.DataType;
import edu.umich.lhs.activator.domain.KnowledgeObject;
import edu.umich.lhs.activator.domain.KnowledgeObjectBuilder;
import edu.umich.lhs.activator.domain.Kobject;
import edu.umich.lhs.activator.domain.KobjectBuilder;
import edu.umich.lhs.activator.domain.Result;
import edu.umich.lhs.activator.exception.ActivatorException;
import edu.umich.lhs.activator.repository.Shelf;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

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

  private static final String SHELF_PATH = "activator.shelf.path";

  // Set the shelf path property
  @BeforeClass
  public static void initializePath() throws IOException {
    String tempFilepath;
    tempFilepath = Files.createTempDirectory("shelf").toString();
    System.setProperty(SHELF_PATH, (tempFilepath != null ? tempFilepath : "tempShelf"));
  }

  @After
  public void tearDown() {
    File folderPath = new File(System.getProperty(SHELF_PATH));

    //Clear the shelf
    if(folderPath.exists() != false){
      String[] itemsOnShelf = folderPath.list();
      for(String file : itemsOnShelf) {
        File currentFile = new File(folderPath.getPath(), file);
        currentFile.delete();
      }
    }

  }

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

    Kobject ko = new KobjectBuilder()
        .addParamDescription("rxcui", DataType.STRING, 1, 1)
        .noofParams(1)
        .returnType(String.class)
        .payloadEngineType("JAVASCRIPT")
        .payloadFunctionName("execute")
        .payloadContent(payload_code)
        .build();
    expectedEx.expect(ActivatorException.class);
    expectedEx.expectMessage("No inputs given.");
    assertNotNull(activationService.validateAndExecute(null, ko));
  }

  //TODO: Move this spec to a test of the Validator
  @Test
  public void testCalculateWithWrongInput() throws Exception {
    Kobject ko = new KobjectBuilder()
        .addParamDescription("rxcui", DataType.STRING, 1, 1)
        .noofParams(1)
        .returnType(String.class)
        .payloadEngineType("JAVASCRIPT")
        .payloadFunctionName("execute")
        .payloadContent(payload_code)
        .build();
    Map<String, Object> inputs = new HashMap<>();
    inputs.put("test", "test");
    expectedEx.expect(ActivatorException.class);
    expectedEx.expectMessage("Input parameter rxcui is missing.");
    assertNotNull(activationService.validateAndExecute(inputs, ko));
  }

  @Test
  public void testCalculateWithCorrectInputsButNoPayload() {
    Kobject ko = new KobjectBuilder()
        .addParamDescription("rxcui", DataType.STRING, 1, 1)
        .noofParams(1)
        .returnType(String.class)
        .payloadEngineType("JAVASCRIPT")
        .payloadFunctionName("execute")
        .build();
    Map<String, Object> inputs = new HashMap<>();
    inputs.put("rxcui", "test");
    Result expectedResult = new Result();
    expectedResult.setSource(null);

    expectedEx.expect(ActivatorException.class);
    expectedEx.expectMessage("Knowledge object payload content is empty");
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
        .noofParams(1)
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
    expectedEx.expectMessage("Number of input parameters should be 1");
    Result generatedResult = activationService.validateAndExecute(inputs, ko);
  }

  //TODO: Move this spec to a test of the Validator
  @Test
  public void testCalculateWithTooFewInputsToThrowEx() {
    Kobject ko = new KobjectBuilder()
        .addParamDescription("rxcui", DataType.INT, 1, 1)
        .addParamDescription("rxcui2", DataType.INT, 1, 1)
        .noofParams(2)
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
    expectedEx.expectMessage("Number of input parameters should be 2\n  Input parameter rxcui2 is missing.");
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
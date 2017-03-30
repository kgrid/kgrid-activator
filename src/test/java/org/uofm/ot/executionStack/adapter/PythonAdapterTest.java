package org.uofm.ot.executionStack.adapter;

import static org.junit.Assert.*;
import static org.mockito.Matchers.startsWith;

import java.util.HashMap;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.uofm.ot.executionStack.exception.OTExecutionStackException;
import org.uofm.ot.executionStack.transferObjects.DataType;
import org.uofm.ot.executionStack.transferObjects.KnowledgeObjectBuilder;
import org.uofm.ot.executionStack.transferObjects.KnowledgeObjectDTO;
import org.uofm.ot.executionStack.transferObjects.Result;

/**
 * Created by nggittle on 3/29/17.
 */
public class PythonAdapterTest {

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Test
  public void executeEmptyPayload() throws Exception {
    PythonAdapter pythonAdapter = new PythonAdapter();
    Map<String, Object> map = new HashMap<>();
    KnowledgeObjectDTO ko = new KnowledgeObjectBuilder().payloadContent("").build();

    expectedEx.expect(OTExecutionStackException.class);
    expectedEx.expectMessage(" function not found in object payload ");
    pythonAdapter.execute(map, ko.payload, DataType.INT);

  }

  @Test
  public void executePayloadWithBadSyntax() throws Exception {
    PythonAdapter pythonAdapter = new PythonAdapter();
    Map<String, Object> map = new HashMap<>();
    KnowledgeObjectDTO ko = new KnowledgeObjectBuilder()
        .payloadContent("def execute:")
        .payloadFunctionName("execute")
        .build();

    expectedEx.expect(OTExecutionStackException.class);
    expectedEx.expectMessage(startsWith("Error while executing payload code SyntaxError:"));
    pythonAdapter.execute(map, ko.payload, DataType.INT);

  }

  @Test
  public void executePayloadWithGoodSyntax() throws Exception {
    PythonAdapter pythonAdapter = new PythonAdapter();
    Map<String, Object> map = new HashMap<>();
    KnowledgeObjectDTO ko = new KnowledgeObjectBuilder()
        .payloadContent("def execute(a):\n     return True")
        .payloadFunctionName("execute")
        .build();

    Result result = new Result();
    result.setResult("1");
    result.setSuccess(0);
    result.setSource(null);
    result.setErrorMessage(null);

    assertEquals(result,pythonAdapter.execute(map, ko.payload, DataType.INT));

  }

}
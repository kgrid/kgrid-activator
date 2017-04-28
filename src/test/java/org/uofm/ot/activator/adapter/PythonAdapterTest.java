package org.uofm.ot.activator.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.uofm.ot.activator.domain.KnowledgeObject;
import org.uofm.ot.activator.domain.KnowledgeObjectBuilder;
import org.uofm.ot.activator.domain.Result;
import org.uofm.ot.activator.exception.OTExecutionStackException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by nggittle on 3/29/17.
 */
public class PythonAdapterTest {

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  private ObjectMapper mapper = new ObjectMapper();

  @Test
  public void executeEmptyPayload() throws Exception {
    PythonAdapter pythonAdapter = new PythonAdapter();
    Map<String, Object> map = new HashMap<>();
    KnowledgeObject ko = new KnowledgeObjectBuilder().payloadContent("").build();

    expectedEx.expect(OTExecutionStackException.class);
    expectedEx.expectMessage(" function not found in object payload ");
    pythonAdapter.execute(map, ko.payload);

  }

  @Test
  public void executePayloadWithBadSyntax() throws Exception {
    PythonAdapter pythonAdapter = new PythonAdapter();
    Map<String, Object> map = new HashMap<>();
    KnowledgeObject ko = new KnowledgeObjectBuilder()
        .payloadContent("def execute:")
        .payloadFunctionName("execute")
        .build();

    expectedEx.expect(OTExecutionStackException.class);
    expectedEx.expectMessage("Error while executing payload code SyntaxError:");
    pythonAdapter.execute(map, ko.payload);

  }

  @Test
  public void executePayloadWithGoodSyntax() throws Exception {
    PythonAdapter pythonAdapter = new PythonAdapter();
    Map<String, Object> map = new HashMap<>();
    KnowledgeObject ko = new KnowledgeObjectBuilder()
        .payloadContent("def execute(a):\n     return True")
        .payloadFunctionName("execute")
        .build();

    Result result = new Result();
    result.setResult(true);
    result.setSource(null);

    assertEquals(result,pythonAdapter.execute(map, ko.payload));

    assertEquals("{\"result\":true,\"source\":null,\"metadata\":null}", mapper.writeValueAsString(result));

  }

  @Test
  public void singleInputValueIsReturned() throws Exception {

    PythonAdapter pyad = new PythonAdapter();

    Map<String, Object> input = new HashMap<>();

    input.put("param1", 42);
    input.put("param2", "Bob");

    KnowledgeObject ko = new KnowledgeObjectBuilder()
            .payloadContent("def execute(input):\n     return input")
            .payloadFunctionName("execute")
            .build();

    Result result = pyad.execute(input, ko.payload);

    assertEquals("Bob", ((Map<String, Object>) result.getResult()).get("param2"));
    assertEquals(42, ((Map<String, Object>) result.getResult()).get("param1"));

    assertEquals("{\"param1\":42,\"param2\":\"Bob\"}", mapper.writeValueAsString(result.getResult()));

  }

  @Test
  public void NullFunctionThrowsException() throws Exception {

    PythonAdapter pyad = new PythonAdapter();

    final Map<String, Object> input = new HashMap<>();
    input.put("param1", null);
//    input.put("param2", "Bob");

    KnowledgeObject ko = new KnowledgeObjectBuilder()
//            .payloadContent(null)
            .payloadContent("def execute(input):\n     return ''")
//            .payloadContent("def execute(input):\n     raise ValueError(\"internal function error\")")
            .payloadFunctionName("execute")
            .build();

    Result result = pyad.execute(input, ko.payload);

    assertEquals("", result.getResult());

  }
}
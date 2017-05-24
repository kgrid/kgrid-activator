package org.uofm.ot.activator.adapter;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.uofm.ot.activator.domain.KnowledgeObject;
import org.uofm.ot.activator.domain.KnowledgeObjectBuilder;
import org.uofm.ot.activator.exception.OTExecutionStackException;

/**
 * Created by nggittle on 5/23/17.
 */
public class JavaScriptAdapterTest {

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();
  JavaScriptAdapter javaScriptAdapter;
  Map<String, Object> argMap;

  @Before
  public void setUp() {
    javaScriptAdapter = new JavaScriptAdapter();
    argMap = new HashMap<>();
  }

  @Test
  public void executeEmptyPayload() throws Exception {
    KnowledgeObject ko = new KnowledgeObjectBuilder().payloadContent("").payloadFunctionName("").build();
    expectedEx.expect(OTExecutionStackException.class);
    expectedEx.expectMessage("Javascript payload is empty or has bad syntax");
    javaScriptAdapter.execute(argMap, ko.payload.content, ko.payload.functionName, Integer.class);
  }

  @Test
  public void executePayloadWithBadSyntax() throws Exception {
    KnowledgeObject ko = new KnowledgeObjectBuilder()
        .payloadContent("function execute():{}")
        .payloadFunctionName("execute")
        .build();

    expectedEx.expect(OTExecutionStackException.class);
    expectedEx.expectMessage("Error occurred while executing javascript code SyntaxError:");
    javaScriptAdapter.execute(argMap, ko.payload.content, ko.payload.functionName, Integer.class);
  }

  @Test
  public void executePayloadWithGoodSyntax() throws Exception {
    KnowledgeObject ko = new KnowledgeObjectBuilder()
        .payloadContent("function execute(a){return 1;}")
        .payloadFunctionName("execute")
        .build();

    Object result = javaScriptAdapter.execute(argMap, ko.payload.content, ko.payload.functionName, Integer.class);

    assertEquals(1, result);
  }

  @Test
  public void executePayloadWithMissingFunction() throws Exception {
    KnowledgeObject ko = new KnowledgeObjectBuilder()
        .payloadContent("function mexecute(a){return 1;}")
        .payloadFunctionName("execute")
        .build();

    expectedEx.expect(OTExecutionStackException.class);
    expectedEx.expectMessage("The function execute was not found in the javascript payload");
    Object result = javaScriptAdapter.execute(argMap, ko.payload.content, ko.payload.functionName, Integer.class);
  }

  @Test
  public void executePayloadWithReturnTypeMismatch() throws Exception {
    KnowledgeObject ko = new KnowledgeObjectBuilder()
        .payloadContent("function execute(a) {return \"true\";}")
        .payloadFunctionName("execute").build();

    expectedEx.expect(OTExecutionStackException.class);
    expectedEx.expectMessage("Type mismatch while converting javascript result to java type");
    Object result = javaScriptAdapter.execute(argMap, ko.payload.content, ko.payload.functionName, Boolean.class);
  }

  @Test
  public void executePayloadWithReturnTypeMatch() throws Exception {
    KnowledgeObject ko = new KnowledgeObjectBuilder()
        .payloadContent("function execute(a){return true}")
        .payloadFunctionName("execute").build();

    Object result = javaScriptAdapter.execute(argMap, ko.payload.content, ko.payload.functionName, Boolean.class);
    assertEquals(new Boolean(true), result);
  }

  @Test
  public void executePayloadWithTwoObjectsInDictionary() throws Exception {
    KnowledgeObject ko = new KnowledgeObjectBuilder()
        .payloadContent("function execute(a){ return a[\"b\"] + a[\"a\"];}")
        .payloadFunctionName("execute").build();
    argMap.put("b", "this is a ");
    argMap.put("a", "test");

    Object result = javaScriptAdapter.execute(argMap, ko.payload.content, ko.payload.functionName, String.class);
    assertEquals("this is a test", result);
  }

  @Test
  public void executePayloadWithInfiniteRecursion() throws Exception {
    KnowledgeObject ko = new KnowledgeObjectBuilder()
        .payloadContent("function execute(a){return execute(a);}")
        .payloadFunctionName("execute").build();

    expectedEx.expect(OTExecutionStackException.class);
    expectedEx.expectMessage("Stack overflow error. Make sure you don't have infinite recursion or memory leaks");
    Object result = javaScriptAdapter.execute(argMap, ko.payload.content, ko.payload.functionName, String.class);
  }


  @Test
  public void executePayloadWithNullMap() throws Exception {
    KnowledgeObject ko = new KnowledgeObjectBuilder()
        .payloadContent("function execute(a){return a[\"b\"];}")
        .payloadFunctionName("execute").build();
    argMap.put("b", null);

    Object result = javaScriptAdapter.execute(argMap, ko.payload.content, ko.payload.functionName, String.class);
    assertEquals(null, result);
  }

  @Test
  public void executePayloadWithFloatIO() throws Exception {
    KnowledgeObject ko = new KnowledgeObjectBuilder()
        .payloadContent("function execute(a){  return a[\"b\"]}")
        .payloadFunctionName("execute").build();
    argMap.put("b", new Float("1.532423444455223553399999331"));

    Object result = javaScriptAdapter.execute(argMap, ko.payload.content, ko.payload.functionName, Float.class);
    assertEquals(new Float("1.532423444455223553399999331"), result);
  }

  @Test
  public void executePayloadWithDoubleIO() throws Exception {
    KnowledgeObject ko = new KnowledgeObjectBuilder()
        .payloadContent("function execute(a){   return a[\"b\"];}")
        .payloadFunctionName("execute").build();
    argMap.put("b", new Double("1.5324234444552235533999993319999999"));

    Object result = javaScriptAdapter.execute(argMap, ko.payload.content, ko.payload.functionName, Double.class);
    assertEquals(new Double("1.5324234444552235533999993319999999"), result);
  }

  @Test
  public void executePayloadWithMapIO() throws Exception {
    KnowledgeObject ko = new KnowledgeObjectBuilder()
        .payloadContent("function execute(a){ return a;}")
        .payloadFunctionName("execute").build();
    argMap.put("b", new Double("1.5324234444552235533999993319999999"));

    Object result = javaScriptAdapter.execute(argMap, ko.payload.content, ko.payload.functionName, Map.class);
    Map<String, Object> exMap = new HashMap<>();
    exMap.put("b", new Double("1.5324234444552235533999993319999999"));
    assertEquals(exMap, result);
  }

  @Test
  public void executePayloadWithNestedMapIO() throws Exception {
    KnowledgeObject ko = new KnowledgeObjectBuilder()
        .payloadContent("function execute(a) { return a;}")
        .payloadFunctionName("execute").build();
    Map<String, Object> inMap = new HashMap<>();
    inMap.put("double", new Double("1.5324234444552235533999993319999999"));
    argMap.put("b", inMap);

    Object result = javaScriptAdapter.execute(argMap, ko.payload.content, ko.payload.functionName, Map.class);
    Map<String, Object> exMap = new HashMap<>();
    exMap.put("double", new Double("1.5324234444552235533999993319999999"));
    assertEquals(exMap, ((Map)result).get("b"));
    exMap.clear();
    exMap.put("b", inMap);
    assertEquals(exMap, result);

  }

}
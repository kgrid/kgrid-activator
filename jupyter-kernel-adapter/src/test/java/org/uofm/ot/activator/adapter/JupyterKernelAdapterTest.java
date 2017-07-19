package org.uofm.ot.activator.adapter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.uofm.ot.activator.adapter.gateway.KernelMetadata;
import org.uofm.ot.activator.adapter.gateway.RestClient;
import org.uofm.ot.activator.adapter.gateway.SockPuppet;
import org.uofm.ot.activator.adapter.gateway.SockResponseProcessor;
import org.uofm.ot.activator.exception.OTExecutionStackException;

/**
 * Created by jadzreik on 2017-06-06.
 */
public class JupyterKernelAdapterTest {

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Mock(name = "restClient")
  private RestClient restClient = mock(RestClient.class);

  @Mock(name = "sockClient")
  private SockPuppet sockClient = mock(SockPuppet.class);

  @Mock(name = "msgProcessor")
  private SockResponseProcessor msgProcessor = mock(SockResponseProcessor.class);

  @InjectMocks
  private JupyterKernelAdapter jupyterKernelAdapter;

  // Params for execute method
  private Map<String, Object> argMap;
  private String payload;
  private String funcName;
  private Class resultClass;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    //default values
    argMap = new HashMap<>();
    payload = "def exec(a):\n    return {\"value\": 1}";
    funcName = "exec";
    resultClass = Map.class;
  }

  private KernelMetadata buildGoodKernel() {
    KernelMetadata goodKernel = new KernelMetadata();
    goodKernel.setName("python7357");
    goodKernel.setId("test-id");

    return goodKernel;
  }

  //
  // Test Kernel Discovery
  //

  @Test
  public void connectToDiscoveredKernel() throws Exception {
    when(restClient.getKernels()).thenReturn(Collections.singletonList(buildGoodKernel()));

    jupyterKernelAdapter.execute(argMap, payload, funcName, resultClass);

    URI expectedUri = URI.create("ws://localhost:8888/api/kernels/test-id/channels");
    verify(sockClient).connectToServer(expectedUri);
  }

  @Test
  public void noKernelAlreadyRunning() throws Exception {
    when(restClient.startKernel()).thenReturn(buildGoodKernel());
    when(restClient.getKernels()).thenReturn(new ArrayList<>());

    jupyterKernelAdapter.execute(argMap, payload, funcName, resultClass);
  }

  @Test
  public void noMatchingKernelAlreadyRunning() throws Exception {
    // Only bad kernel in running kernels
    KernelMetadata badKernel = new KernelMetadata();
    badKernel.setName("badKernel");
    when(restClient.getKernels()).thenReturn(Collections.singletonList(badKernel));

    // Return good kernel when asked to start a new one
    when(restClient.startKernel()).thenReturn(buildGoodKernel());

    jupyterKernelAdapter.execute(argMap, payload, funcName, resultClass);
    verify(restClient).startKernel();
  }

  @Test
  public void noKernelsAvailable() throws Exception {
    when(restClient.startKernel()).thenReturn(null);
    when(restClient.getKernels()).thenReturn(new ArrayList<>());

    expectedEx.expect(OTExecutionStackException.class);
    expectedEx.expectMessage(" No available Jupyter Kernel for payload ");

    jupyterKernelAdapter.execute(argMap, payload, funcName, resultClass);
  }

  //
  // Test Response Handling
  //
  @Test
  public void resultReceived() throws Exception {
    when(restClient.getKernels()).thenReturn(Collections.singletonList(buildGoodKernel()));
    Map<String, Object> retVal = new LinkedHashMap<>();
    when(msgProcessor.getResult()).thenReturn(retVal);

    Object result = jupyterKernelAdapter.execute(argMap, payload, funcName, resultClass);
    assertThat(result, sameInstance(retVal));
  }

  @Test
  public void errorReceived() throws Exception {
    when(restClient.getKernels()).thenReturn(Collections.singletonList(buildGoodKernel()));
    String errMsg = "Testing error message";
    when(msgProcessor.encounteredError()).thenReturn(true);
    when(msgProcessor.getErrorMsg()).thenReturn(errMsg);

    expectedEx.expect(OTExecutionStackException.class);
    expectedEx.expectMessage(errMsg);

    jupyterKernelAdapter.execute(argMap, payload, funcName, resultClass);
  }

  @Test
  public void processingResponseTimeout() throws Exception {
    when(restClient.getKernels()).thenReturn(Collections.singletonList(buildGoodKernel()));
    when(msgProcessor.encounteredError()).thenReturn(false);
    when(msgProcessor.encounteredTimeout()).thenReturn(true);

    expectedEx.expect(OTExecutionStackException.class);
    expectedEx.expectMessage("Timeout occurred");

    jupyterKernelAdapter.execute(argMap, payload, funcName, resultClass);
  }

  //
  // Argument validation
  //

  @Test
  public void emptyPayload() throws Exception {
    expectedEx.expect(OTExecutionStackException.class);
    expectedEx.expectMessage("No code to execute");
    jupyterKernelAdapter.execute(argMap, "", "foo", resultClass);
  }

  @Test
  public void emptyFunctionName() throws Exception {
    expectedEx.expect(OTExecutionStackException.class);
    expectedEx.expectMessage("No function name to execute");
    jupyterKernelAdapter.execute(argMap, payload, "", resultClass);

  }

}
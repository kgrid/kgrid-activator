package org.uofm.ot.activator.adapter;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.uofm.ot.activator.adapter.gateway.KernelMetadata;
import org.uofm.ot.activator.adapter.gateway.RestClient;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.uofm.ot.activator.adapter.gateway.SockPuppet;
import org.uofm.ot.activator.adapter.gateway.WebSockMessage;
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
  private ArrayBlockingQueue<WebSockMessage> messageQ;

  @InjectMocks
  private JupyterKernelAdapter jupyterKernelAdapter;

  private Map<String, Object> argMap;
  String payload;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    argMap = new HashMap<>();
    payload = "def exec(a):\n    return 1";
    messageQ = new ArrayBlockingQueue<WebSockMessage>(5);
    when(sockClient.getMessageQ()).thenReturn(messageQ);

    addResponseToQueue("1");
  }

  // Add result to mocked message queue from web socket client
  private void addResponseToQueue(String val){
    WebSockMessage result = new WebSockMessage();
    result.messageType = "stream";
    result.content.text = val;
    messageQ.add(result);
  }

  private KernelMetadata getGoodKernel(){
    KernelMetadata goodKernel = new KernelMetadata();
    goodKernel.setName("python7357");
    goodKernel.setId("test-id");

    return goodKernel;
  }

  @Test
  public void executeEmptyPayload() throws Exception {
    expectedEx.expect(OTExecutionStackException.class);
    expectedEx.expectMessage("No code to execute");
    jupyterKernelAdapter.execute(argMap, "", "", Integer.class);
  }

  @Test
  public void executeSimplePayload() throws Exception {
    when(restClient.getKernels()).thenReturn(Collections.singletonList(getGoodKernel()));

    Object result = jupyterKernelAdapter.execute(argMap, payload, "exec", Integer.class);
    assertThat(result, equalTo(1));
  }

  @Test
  public void connectToDiscoveredKernel() throws Exception {
    when(restClient.getKernels()).thenReturn(Collections.singletonList(getGoodKernel()));

    jupyterKernelAdapter.execute(argMap, payload, "exec", Integer.class);

    URI expectedUri = URI.create("ws://localhost:8888/api/kernels/test-id/channels");
    verify(sockClient).connectToServer(expectedUri);
  }

  @Test
  public void noKernelAlreadyRunning() throws Exception {
    when(restClient.startKernel()).thenReturn(getGoodKernel());
    when(restClient.getKernels()).thenReturn(new ArrayList<>());

    jupyterKernelAdapter.execute(argMap, payload, "exec", Integer.class);
  }

  @Test
  public void noMatchingKernelAlreadyRunning() throws Exception {
    // Only bad kernel in running kernels
    KernelMetadata badKernel = new KernelMetadata();
    badKernel.setName("badKernel");
    when(restClient.getKernels()).thenReturn(Collections.singletonList(badKernel));

    // Return good kernel when asked to start a new one
    when(restClient.startKernel()).thenReturn(getGoodKernel());

    jupyterKernelAdapter.execute(argMap, payload, "exec", Integer.class);
    verify(restClient).startKernel();
  }

  @Test
  public void noKernelsAvailable() throws Exception {
    when(restClient.startKernel()).thenReturn(null);
    when(restClient.getKernels()).thenReturn(new ArrayList<>());

    expectedEx.expect(OTExecutionStackException.class);
    expectedEx.expectMessage(" no available Jupyter Kernel for payload ");
    jupyterKernelAdapter.execute(argMap, payload, "exec", Integer.class);
  }


}
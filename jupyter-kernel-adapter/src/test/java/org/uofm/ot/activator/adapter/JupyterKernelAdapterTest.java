package org.uofm.ot.activator.adapter;

import java.util.ArrayList;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.uofm.ot.activator.adapter.gateway.KernelMetadata;
import org.uofm.ot.activator.adapter.gateway.RestClient;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.uofm.ot.activator.adapter.gateway.RestClient;
import org.uofm.ot.activator.exception.OTExecutionStackException;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by jadzreik on 2017-06-06.
 */
public class JupyterKernelAdapterTest {

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Mock(name = "restClient")
  private RestClient mockClient = mock(RestClient.class);

  @InjectMocks
  private JupyterKernelAdapter jupyterKernelAdapter;

  private Map<String, Object> argMap;

  @Before
  public void setUp() {
    jupyterKernelAdapter = new JupyterKernelAdapter();
    argMap = new HashMap<>();
  }

  @Test
  public void executeEmptyPayload() throws Exception {
    expectedEx.expect(OTExecutionStackException.class);
    expectedEx.expectMessage(" function not found in object payload ");
    jupyterKernelAdapter.execute(argMap, "", "", Integer.class);
  }


  @Test
  public void noKernelsAvailable() throws Exception {
    String payload = "def exec(a):\n    return 1";
    when(mockClient.startKernel()).thenReturn("");
    when(mockClient.getKernels()).thenReturn(new ArrayList<>());

    expectedEx.expect(OTExecutionStackException.class);
    expectedEx.expectMessage(" no available Jupyter Kernels for payload ");
    jupyterKernelAdapter.execute(argMap, payload, "exec", Integer.class);
  } 

}
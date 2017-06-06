package org.uofm.ot.activator.adapter;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.uofm.ot.activator.exception.OTExecutionStackException;

/**
 * Created by jadzreik on 2017-06-06.
 */
public class JupyterKernelAdapterTest {

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

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
  public void jupyterNotFound() throws Exception {
    String payload = "def exec(a):\n    return 1";
    expectedEx.expect(OTExecutionStackException.class);
    expectedEx.expectMessage(" unable to connect to Jupyter Kernel ");
    jupyterKernelAdapter.execute(argMap, payload, "exec", Integer.class);
  } 

    @Test
  public void jupyterFound() throws Exception {
    String payload = "def exec(a):\n    return 1";
    Object result = jupyterKernelAdapter.execute(argMap, payload, "exec", Integer.class);
    assertEquals(result, 1); 
  } 



}
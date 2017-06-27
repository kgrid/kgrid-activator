package org.uofm.ot.activator.adapter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import java.util.HashMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

/**
 * Created by grosscol on 2017-06-20.
 */
@Category(org.uofm.ot.activator.adapter.IntegrationTest.class)
public class JupyterKernelIntegrationTest {

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Test
  public void runHello() throws Exception {
    String code = TestUtils.loadFixture("payload-hello-no-params.py");
    JupyterKernelAdapter adapter = new JupyterKernelAdapter();
    Object result = adapter.execute(new HashMap<>(), code, "hello", String.class  );
    assertThat(result, equalTo("Hello, World\n") );
  }

  @Test
  public void runNumbers() throws Exception {
    String code = TestUtils.loadFixture("payload-numbers-no-params.py");
    JupyterKernelAdapter adapter = new JupyterKernelAdapter();
    Object result = adapter.execute(new HashMap<>(), code, "numbers", Integer.class  );
    assertThat(result, equalTo("101") );
  }
}

package org.kgrid.activator.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.kgrid.activator.services.RepoUtils.C_D_F;
import static org.kgrid.activator.services.RepoUtils.getBinaryTestFile;
import static org.kgrid.activator.services.RepoUtils.getYamlTestFile;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.kgrid.activator.ActivatorException;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.Executor;
import org.kgrid.adapter.javascript.JavascriptAdapter;
import org.kgrid.shelf.repository.CompoundDigitalObjectStore;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class EndpointActivationTests {

  public static final String C_D_F_WELCOME = C_D_F.getDashArkImplementation() + "/welcome";
  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  @Mock
  AdapterService adapterService;
  @Mock
  Adapter adapter;
  @Mock
  CompoundDigitalObjectStore cdoStore;
  @InjectMocks
  ActivationService activationService;
  private byte[] payload;
  private JsonNode dep;

  @Before
  public void setUp() throws Exception {
    dep = getYamlTestFile(C_D_F.getDashArkImplementation(), "deployment.yaml");
    payload = getBinaryTestFile(C_D_F.getDashArkImplementation(), "welcome.js");
  }

  @Test
  public void activateFindsAdapterAndActivatesEndpoint() throws IOException {

    given(adapterService.findAdapter("JAVASCRIPT"))
        .willReturn(adapter);

    given(adapter.activate(any(), any()))
        .willReturn(new Executor() {
          @Override
          public Object execute(Object input) {
            return null;
          }
        });

    final Endpoint endpoint = Endpoint.Builder
        .anEndpoint()
        .withDeployment(dep.get("endpoints").get("/welcome")).build();

    // when
    Executor executor = activationService.activate(C_D_F_WELCOME, endpoint);

    assertNotNull("Executor should not be null", executor);

    then(adapterService).should().findAdapter("JAVASCRIPT");

    then(adapter).should().activate(
        Paths.get(C_D_F.getDashArkImplementation(), "/welcome.js"),
        "welcome");

  }

  @Test
  public void activateCreatesWorkingExecutor() {

    final JavascriptAdapter adapter = new JavascriptAdapter();
    adapter.initialize(new Properties());
    adapter.setCdoStore(cdoStore);

    given(cdoStore.getBinary(any())).willReturn(payload);

    given(adapterService.findAdapter("JAVASCRIPT"))
        .willReturn(adapter);

    final Endpoint endpoint = Endpoint.Builder
        .anEndpoint()
        .withDeployment(dep.get("endpoints").get("/welcome"))
        .build();

    // when
    Executor executor = activationService.activate(C_D_F_WELCOME, endpoint);

    assertNotNull("Executor should not be null", executor);

    Object output = executor.execute(new HashMap<String, String>() {{
      put("name", "Bob");
    }});

    assertEquals("Welcome to Knowledge Grid, Bob", output);
  }

  @Test
  public void endpointWithNoMatchingDeploymentSpecThrowsActivationException() {
    expectedException.expect(ActivatorException.class);
    expectedException.expectMessage("No deployment specification");

    final Endpoint endpoint = Endpoint.Builder
        .anEndpoint()
        .withDeployment(null)
        .build();

    // when
    Executor executor = activationService.activate(C_D_F_WELCOME, endpoint);
  }

}
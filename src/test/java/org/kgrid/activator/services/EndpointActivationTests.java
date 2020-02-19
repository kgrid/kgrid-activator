package org.kgrid.activator.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.kgrid.activator.utils.RepoUtils.C_D_F;
import static org.kgrid.activator.utils.RepoUtils.getBinaryTestFile;
import static org.kgrid.activator.utils.RepoUtils.getYamlTestFile;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.reset;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.kgrid.activator.ActivatorException;
import org.kgrid.adapter.api.ActivationContext;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.Executor;
import org.kgrid.adapter.javascript.JavascriptAdapter;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.repository.CompoundDigitalObjectStore;
import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class EndpointActivationTests {

  public static final EndpointId C_D_F_WELCOME = new EndpointId(C_D_F, "/welcome");
  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  @Mock
  AdapterResolver adapterResolver;
  @Mock
  Adapter adapter;
  @Mock
  CompoundDigitalObjectStore cdoStore;
  @Mock
  KnowledgeObjectRepository koRepo;

  ActivationContext context = new ActivationContext() {
    @Override
    public Executor getExecutor(String key) {
      return null;
    }

    @Override
    public byte[] getBinary(String pathToBinary) {
      return cdoStore.getBinary(pathToBinary);
    }

    @Override
    public String getProperty(String key) {
      return null;
    }
  };

  @InjectMocks
  ActivationService activationService;
  private byte[] payload;
  private JsonNode dep;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    dep = getYamlTestFile(C_D_F.getDashArk() + "-" + C_D_F.getVersion(), "deployment.yaml");
    payload = getBinaryTestFile(C_D_F.getDashArk() + "-" + C_D_F.getVersion(), "welcome.js");
  }

  @Test
  public void activateFindsAdapterAndActivatesEndpoint() throws IOException {

    given(adapterResolver.getAdapter("JAVASCRIPT"))
        .willReturn(adapter);

    given(koRepo.getObjectLocation(new ArkId("c-d/f")))
        .willReturn("c-d-f");

    given(adapter.activate(any(), any(), any(JsonNode.class)))
        .willReturn(new Executor() {
          @Override
          public Object execute(Object input) {
            return null;
          }
        });

    given(adapter.activate(any(), any(String.class)))
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

    then(adapterResolver).should()
        .getAdapter("JAVASCRIPT");

    then(adapter).should().activate(
        C_D_F.getDashArk() + "-" + C_D_F.getVersion(), C_D_F,
        dep.get("endpoints").get("/welcome"));

  }

  @Test
  public void activateCreatesWorkingExecutor() {

    final JavascriptAdapter adapter = new JavascriptAdapter();
    adapter.initialize(context);

    given(cdoStore.getBinary(any())).willReturn(payload);

    given(adapterResolver.getAdapter("JAVASCRIPT"))
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

  @Test
  public void activatingEndpointWithMissingArtifactThrowsActivatorException() {
    expectedException.expect(ActivatorException.class);
    expectedException.expectMessage("Binary resource not found");

    final Endpoint endpoint = Endpoint.Builder.anEndpoint()
        .withDeployment(dep.get("endpoints").get("/welcome")) // test deployment file
        .build();

    given(koRepo.getObjectLocation(new ArkId("c-d/f"))).willReturn("c-d");

    given(adapterResolver.getAdapter("JAVASCRIPT"))
        .willReturn(adapter);

    given(adapter.activate(any(String.class), any(), any()))
        .willThrow(new AdapterException("Binary resource not found..."));

    // when
    Executor executor = activationService.activate(C_D_F_WELCOME, endpoint);
  }

  @Test
  public void activatingEndpointWithMissingArtifactGivesNullExecutor() {

    final Endpoint endpoint = Endpoint.Builder.anEndpoint()
        .withDeployment(dep.get("endpoints").get("/welcome")) // test deployment file
        .build();

    given(adapterResolver.getAdapter("JAVASCRIPT"))
        .willReturn(adapter);

    given(adapter.activate(any(String.class), any(), any(JsonNode.class)))
        .willThrow(new AdapterException("Binary resource not found..."));

    // when
    activationService.activate(new HashMap<EndpointId, Endpoint>() {{
      put(C_D_F_WELCOME, endpoint);
    }});

    assertNull("Executor should not be available",
        endpoint.getExecutor());

    // try again with a real Executor returned
    reset(adapter);
    given(adapter.activate(any(String.class), any(), any(JsonNode.class)))
        .willReturn(new Executor() {
          @Override
          public Object execute(Object input) {
            return input;
          }
        });

    // when
    activationService.activate(new HashMap<EndpointId, Endpoint>() {{
      put(C_D_F_WELCOME, endpoint);
    }});

    // no exception and it's the configured executor
//    assertNotNull(endpoint.getExecutor());
//    assertEquals("foo", endpoint.getExecutor().execute("foo"));
  }
}

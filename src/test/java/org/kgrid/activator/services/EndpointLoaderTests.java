package org.kgrid.activator.services;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.kgrid.activator.utils.RepoUtils.A_B_C;
import static org.kgrid.activator.utils.RepoUtils.C_D_E;
import static org.kgrid.activator.utils.RepoUtils.C_D_F;
import static org.kgrid.activator.utils.RepoUtils.getJsonTestFile;
import static org.kgrid.activator.utils.RepoUtils.getYamlTestFile;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kgrid.activator.EndpointLoader;
import org.kgrid.shelf.ShelfResourceNotFound;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.domain.KnowledgeObject;
import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
//@SpringBootTest
public class EndpointLoaderTests {

  public static final String IMPL = KnowledgeObject.IMPLEMENTATIONS_TERM;

  @Mock
  private KnowledgeObjectRepository repository;

  @InjectMocks
  private EndpointLoader endpointLoader;

  @Before
  public void setUp() throws Exception {
    loadMockRepoWithKOs();
    loadMockRepoWithImplementations();
    loadMockRepoWithServiceSpecs();
    loadMockRepoWithDeploymentSpecs();
  }

  @Test
  public void endpointIsLoadedForAnImplementation() throws IOException {

    // load single endpoint implementation
    Map<EndpointId, Endpoint> eps = endpointLoader.load(A_B_C);
    Endpoint endpoint = eps.get(new EndpointId(A_B_C,"/welcome"));

    assertNotNull("endpointPath 'a-b/c/welcome' should exist", endpoint);

    // test that endpoint parts exists
    assertNotNull("service descriptor should exist", endpoint.getService());
    assertNotNull("deployment spec should exist", endpoint.getDeployment());
    assertNotNull("metadata should exist", endpoint.getMetadata());

    // test deployment descriptor example
    JsonNode deploymentSpec = getYamlTestFile(A_B_C.getDashArk() + "-" + A_B_C.getVersion(), "deployment.yaml");

    assertEquals("endpoint path exists in deployment descriptor",
        endpoint.getDeployment().toString(),
        deploymentSpec.get("endpoints").get("/welcome").toString());

    assertNotNull("enpoint spec 'a-b-c/info' is in original spec",
        deploymentSpec.get("endpoints").get("/info"));

    endpoint = eps.get(A_B_C.getDashArk() + "-" + A_B_C.getVersion() + "/info");
    assertNull("endpointPath 'a-b-c/info' should not exist", endpoint);
  }

  @Test
  public void endpointIsLoadedForAnKO() throws IOException {

    // load single endpoint implementation
    Map<EndpointId, Endpoint> eps = endpointLoader.load(C_D_F);
    Endpoint endpoint = eps.get(new EndpointId(C_D_F,"/welcome"));

    System.out.println( endpoint );

    assertEquals("should load four end points",3, eps.size());

    assertNotNull("endpointPath 'c-d-f/welcome' should exist", endpoint);

    // test that endpoint parts exists
    assertNotNull("service descriptor should exist", endpoint.getService());
    assertNotNull("deployment spec should exist", endpoint.getDeployment());
    assertNotNull("metadata should exist", endpoint.getMetadata());

  }

  @Test
  public void activationPopulatesEndpoints() throws IOException {

    // when
    Map<EndpointId, Endpoint> eps = endpointLoader.load();

    // Loader methods load 2 KOs, with 3 impls, and 5 endpoints (1 service spec has 3 endpoints!)
    assertEquals("Map should have 5 endpoints", 5, eps.size());

    assertNotNull("'a-b-c/welcome' exists", eps.get(new EndpointId(A_B_C, "/welcome")));
    assertNotNull("'c-d-e/welcome' exists", eps.get(new EndpointId(C_D_E, "/welcome")));
    assertNotNull("'c-d-f/welcome' exists", eps.get(new EndpointId(C_D_F, "/welcome")));
    assertNotNull("'c-d-f/goodbye' exists", eps.get(new EndpointId(C_D_F, "/goodbye")));
    assertNotNull("'c-d-f/info' exists", eps.get(new EndpointId(C_D_F, "/info")));
  }

  @Test
  public void serviceLoadsImplementationMetadata() throws IOException {

    // when
    Map<EndpointId, Endpoint> endpoints = endpointLoader.load();

//    then(repository).should().findKnowledgeObjectMetadata(A_B_C);
//    then(repository).should().findKnowledgeObjectMetadata(C_D_E);
//    then(repository).should().findKnowledgeObjectMetadata(C_D_F);

  }

  @Test
  public void endpointsContainServices() throws IOException {

    // when
    Map<EndpointId, Endpoint> endpoints = endpointLoader.load();

    endpoints.forEach((path, endpoint) -> {
      final JsonNode service = endpoint.getService();
      assertNotNull("Service spec must exist", service);
      assertThat("Service spec must have endpoint path(service)",
          service.toString(),
          hasJsonPath("paths")
      );
      String endpointKey = path.getEndpointName();
      assertNotNull("Service contains endpoint path", service.get("paths").get(endpointKey));
    });
  }

  @Test
  public void missingServiceSpecLogsAndSkips() {

    when(repository.findServiceSpecification(A_B_C,
        repository.findKnowledgeObjectMetadata(A_B_C)))
        .thenThrow(ShelfResourceNotFound.class);

    assertNull(endpointLoader.load(A_B_C).get(new EndpointId(A_B_C, "/welcome")));

    assertNull(endpointLoader.load().get(new EndpointId(A_B_C, "/welcome")));

  }

  @Test
  public void missingImplementationLogsAndSkips() {

    given(repository.findKnowledgeObjectMetadata(A_B_C))
        .willThrow(ShelfResourceNotFound.class);

    assertNull(endpointLoader.load(A_B_C).get(new EndpointId(A_B_C, "/welcome")));

    assertNull(endpointLoader.load().get(new EndpointId(A_B_C, "/welcome")));

  }
  /*
   * Test loader methods
   */
  private void loadMockRepoWithKOs() throws IOException {
    // All KOs on shelf (A_B, C_D)
    final Map<ArkId, JsonNode> kos = new HashMap<>();
    kos.put(A_B_C, getJsonTestFile(A_B_C.getDashArk() + "-" + A_B_C.getVersion(), "metadata.json"));
    kos.put(C_D_E, getJsonTestFile(C_D_E.getDashArk() + "-" + C_D_E.getVersion(), "metadata.json"));
    kos.put(C_D_F, getJsonTestFile(C_D_F.getDashArk() + "-" + C_D_F.getVersion(), "metadata.json"));

    given(repository.findAll()).willReturn(kos);
    given(repository.findKnowledgeObjectMetadata(C_D_E)).willReturn(
        getJsonTestFile(C_D_E.getDashArk() + "-" + C_D_E.getVersion(), "metadata.json"));
  }

  private void loadMockRepoWithServiceSpecs() throws IOException {
    //service specs
    given(repository.findServiceSpecification(eq(A_B_C), any()))
        .willReturn(getYamlTestFile(A_B_C.getDashArk() + "-" + A_B_C.getVersion(), "service.yaml"));
    given(repository.findServiceSpecification(eq(C_D_E), any()))
        .willReturn(getYamlTestFile(C_D_E.getDashArk() + "-" + C_D_E.getVersion(), "service.yaml"));
    given(repository.findServiceSpecification(eq(C_D_F), any()))
        .willReturn(getYamlTestFile(C_D_F.getDashArk() + "-" + C_D_F.getVersion(), "service.yaml"));
  }

  private void loadMockRepoWithImplementations() throws IOException {
    // implementations for those KOs
    given(repository.findKnowledgeObjectMetadata(eq(A_B_C)))
        .willReturn(getJsonTestFile(A_B_C.getDashArk() + "-" + A_B_C.getVersion(), "metadata.json"));
    given(repository.findKnowledgeObjectMetadata(eq(C_D_E)))
        .willReturn(getJsonTestFile(C_D_E.getDashArk() + "-" + C_D_E.getVersion() , "metadata.json"));
    given(repository.findKnowledgeObjectMetadata(eq(C_D_F)))
        .willReturn(getJsonTestFile(C_D_F.getDashArk() + "-" + C_D_F.getVersion(), "metadata.json"));
  }

  private void loadMockRepoWithDeploymentSpecs() throws IOException {
    // implementations for those KOs
    given(repository.findDeploymentSpecification(eq(A_B_C), any()))
        .willReturn(getYamlTestFile(A_B_C.getDashArk() + "-" + A_B_C.getVersion(), "deployment.yaml"));
    given(repository.findDeploymentSpecification(eq(C_D_E), any()))
        .willReturn(getYamlTestFile(C_D_E.getDashArk() + "-" + C_D_E.getVersion(), "deployment.yaml"));
    given(repository.findDeploymentSpecification(eq(C_D_F), any()))
        .willReturn(getYamlTestFile(C_D_F.getDashArk() + "-" + C_D_F.getVersion(), "deployment.yaml"));
  }
}

package org.kgrid.activator.services;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.kgrid.activator.utils.RepoUtils.A_B;
import static org.kgrid.activator.utils.RepoUtils.A_B_C;
import static org.kgrid.activator.utils.RepoUtils.C_D;
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
    Map<String, Endpoint> eps = endpointLoader.load(A_B_C);
    Endpoint endpoint = eps.get(A_B_C.getDashArkImplementation() + "/welcome");

    assertNotNull("endpointPath 'a-b/c/welcome' should exist", endpoint);

    // test that endpoint parts exists
    assertNotNull("service descriptor should exist", endpoint.getService());
    assertNotNull("deployment spec should exist", endpoint.getDeployment());
    assertNotNull("implementation should exist", endpoint.getImpl());

    // test deployment descriptor example
    JsonNode deploymentSpec = getYamlTestFile(A_B_C.getDashArkImplementation(), "deployment.yaml");

    assertEquals("endpoint path exists in deployment descriptor",
        endpoint.getDeployment().toString(),
        deploymentSpec.get("endpoints").get("/welcome").toString());

    assertNotNull("enpoint spec 'a-b/c/info' is in original spec",
        deploymentSpec.get("endpoints").get("/info"));

    endpoint = eps.get(A_B_C.getDashArkImplementation() + "/info");
    assertNull("endpointPath 'a-b/c/info' should not exist", endpoint);
  }

  @Test
  public void activationPopulatesEndpoints() throws IOException {

    // when
    Map<String, Endpoint> eps = endpointLoader.load();

    // Loader methods load 2 KOs, with 3 impls, and 5 endpoints (1 service spec has 3 endpoints!)
    assertEquals("Map should have 5 endpoints", 5, eps.size());

    assertNotNull("'a-b/c/welcome' exists", eps.get("a-b/c/welcome"));
    assertNotNull("'c-d/e/welcome' exists", eps.get("c-d/e/welcome"));
    assertNotNull("'c-d/f/welcome' exists", eps.get("c-d/f/welcome"));
    assertNotNull("'c-d/f/goodbye' exists", eps.get("c-d/f/goodbye"));
    assertNotNull("'c-d/f/info' exists", eps.get("c-d/f/info"));
  }

  @Test
  public void serviceLoadsImplementationMetadata() throws IOException {

    // when
    Map<String, Endpoint> endpoints = endpointLoader.load();

    then(repository).should().findImplementationMetadata(A_B_C);
    then(repository).should().findImplementationMetadata(C_D_E);
    then(repository).should().findImplementationMetadata(C_D_F);

    assertThat(endpoints.get(A_B_C.getDashArkImplementation() + "/welcome").getImpl().toString(),
        hasJsonPath(KnowledgeObject.PAYLOAD_TERM));
  }

  @Test
  public void endpointsContainServices() throws IOException {

    // when
    Map<String, Endpoint> endpoints = endpointLoader.load();

    endpoints.forEach((path, endpoint) -> {
      final JsonNode service = endpoint.getService();
      assertNotNull("Service spec must exist", service);
      assertThat("Service spec must have endpoint path(service)",
          service.toString(),
          hasJsonPath("paths")
      );
      String endpointKey = "/" + StringUtils.substringAfterLast(path, "/");
      assertNotNull("Service contains endpoint path", service.get("paths").get(endpointKey));
    });
  }

  @Test
  public void missingServiceSpecLogsAndSkips() {

    when(repository.findServiceSpecification(A_B_C,
        repository.findImplementationMetadata(A_B_C)))
        .thenThrow(ShelfResourceNotFound.class);

    assertNull(endpointLoader.load(A_B_C).get(A_B_C.getDashArkImplementation() + "/welcome"));

    assertNull(endpointLoader.load().get(A_B_C.getDashArkImplementation() + "/welcome"));

  }

  @Test
  public void missingDeploymentSpecLogsAndSkips() {

    when(repository.findDeploymentSpecification(A_B_C,
        repository.findImplementationMetadata(A_B_C)))
        .thenThrow(ShelfResourceNotFound.class);

    assertNull(endpointLoader.load(A_B_C).get(A_B_C.getDashArkImplementation() + "/welcome"));

    assertNull(endpointLoader.load().get(A_B_C.getDashArkImplementation() + "/welcome"));

  }

  @Test
  public void missingImplementationLogsAndSkips() {

    given(repository.findImplementationMetadata(A_B_C))
        .willThrow(ShelfResourceNotFound.class);

    assertNull(endpointLoader.load(A_B_C).get(A_B_C.getDashArkImplementation() + "/welcome"));

    assertNull(endpointLoader.load().get(A_B_C.getDashArkImplementation() + "/welcome"));

  }
  /*
   * Test loader methods
   */
  private void loadMockRepoWithKOs() throws IOException {
    // All KOs on shelf (A_B, C_D)
    final Map<ArkId, JsonNode> kos = new HashMap<>();
    kos.put(A_B, getJsonTestFile(A_B.getDashArk(), "metadata.json"));
    kos.put(C_D, getJsonTestFile(C_D.getDashArk(), "metadata.json"));

    given(repository.findAll()).willReturn(kos);
  }

  private void loadMockRepoWithServiceSpecs() throws IOException {
    //service specs
    given(repository.findServiceSpecification(eq(A_B_C), any()))
        .willReturn(getYamlTestFile(A_B_C.getDashArkImplementation(), "service.yaml"));
    given(repository.findServiceSpecification(eq(C_D_E), any()))
        .willReturn(getYamlTestFile(C_D_E.getDashArkImplementation(), "service.yaml"));
    given(repository.findServiceSpecification(eq(C_D_F), any()))
        .willReturn(getYamlTestFile(C_D_F.getDashArkImplementation(), "service.yaml"));
  }

  private void loadMockRepoWithImplementations() throws IOException {
    // implementations for those KOs
    given(repository.findImplementationMetadata(eq(A_B_C)))
        .willReturn(getJsonTestFile(A_B_C.getDashArkImplementation(), "metadata.json"));
    given(repository.findImplementationMetadata(eq(C_D_E)))
        .willReturn(getJsonTestFile(C_D_E.getDashArkImplementation(), "metadata.json"));
    given(repository.findImplementationMetadata(eq(C_D_F)))
        .willReturn(getJsonTestFile(C_D_F.getDashArkImplementation(), "metadata.json"));
  }

  private void loadMockRepoWithDeploymentSpecs() throws IOException {
    // implementations for those KOs
    given(repository.findDeploymentSpecification(eq(A_B_C), any()))
        .willReturn(getYamlTestFile(A_B_C.getDashArkImplementation(), "deployment.yaml"));
    given(repository.findDeploymentSpecification(eq(C_D_E), any()))
        .willReturn(getYamlTestFile(C_D_E.getDashArkImplementation(), "deployment.yaml"));
    given(repository.findDeploymentSpecification(eq(C_D_F), any()))
        .willReturn(getYamlTestFile(C_D_F.getDashArkImplementation(), "deployment.yaml"));
  }
}
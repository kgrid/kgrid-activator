package org.kgrid.activator.services;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kgrid.activator.EndpointLoader;
import org.kgrid.shelf.ShelfResourceNotFound;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.domain.KnowledgeObjectWrapper;
import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.kgrid.activator.utils.RepoUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
public class EndpointLoaderTests {

    @Mock
    private KnowledgeObjectRepository repository;

    @Mock
    private KoValidationService koValidationService;

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

        Map<URI, Endpoint> eps = endpointLoader.load(A_B_C);
        Endpoint endpoint = eps.get(URI.create("a/b/c/welcome"));

        assertNotNull("endpointPath 'a/b/c/welcome' should exist", endpoint);

        assertNotNull("service descriptor should exist", endpoint.getService());
        assertNotNull("deployment spec should exist", endpoint.getDeployment());
        assertNotNull("metadata should exist", endpoint.getMetadata());

        JsonNode deploymentSpec = getYamlTestFile(A_B_C, "deployment.yaml");

        assertEquals("endpoint path exists in deployment descriptor",
                endpoint.getDeployment().toString(),
                deploymentSpec.get("/welcome").get("post").toString());

        assertNotNull("enpoint spec 'a-b-c/info' is in original spec",
                deploymentSpec.get("/info").get("post"));

        endpoint = eps.get(URI.create("a/b/c/info"));
        assertNull("endpointPath 'a-b-c/info' should not exist", endpoint);
    }

    @Test
    public void endpointIsLoadedForAnKO() {

        // load single endpoint implementation
        Map<URI, Endpoint> eps = endpointLoader.load(C_D_F);

        URI epUri = URI.create(C_D_F.getSlashArkVersion()+"/").resolve("welcome");
        Endpoint endpoint = eps.get(epUri);

        assertEquals("should load 3 end points", 3, eps.size());
        assertNotNull("endpointPath 'c-d-f/welcome' should exist", endpoint);

        assertNotNull("service descriptor should exist", endpoint.getService());
        assertNotNull("deployment spec should exist", endpoint.getDeployment());
        assertNotNull("metadata should exist", endpoint.getMetadata());

    }

    @Test
    public void activationPopulatesEndpointsWithMultipleVersions() {

        Map<URI, Endpoint> eps = endpointLoader.load();

        assertEquals("Map should have 5 endpoints", 5, eps.size());

        assertNotNull("'a-b-c/welcome' exists", eps.get(URI.create("a/b/c/welcome")));
        assertNotNull("'c-d-e/welcome' exists", eps.get(URI.create("c/d/e/welcome")));
        assertNotNull("'c-d-f/welcome' exists", eps.get(URI.create("c/d/f/welcome")));
        assertNotNull("'c-d-f/goodbye' exists", eps.get(URI.create("c/d/f/goodbye")));
        assertNotNull("'c-d-f/info' exists", eps.get(URI.create("c/d/f/info")));
    }


    @Test
    public void endpointsContainServices() {

        Map<URI, Endpoint> endpoints = endpointLoader.load();

        endpoints.forEach((path, endpoint) -> {
            final JsonNode service = endpoint.getService();
            assertNotNull("Service spec must exist", service);
            assertTrue("Service spec must have endpoint path(service)", service.has("paths"));
            String endpointKey = endpoint.getEndpointName();
            assertNotNull("Service contains endpoint path", service.get("paths").get(endpointKey));
        });
    }

    @Test
    public void missingImplementationLogsAndSkips() {

        given(repository.getKow(A_B_C))
                .willThrow(ShelfResourceNotFound.class);

        assertNull(endpointLoader.load(A_B_C).get(URI.create("a/b/c/welcome")));

        assertNull(endpointLoader.load().get(URI.create("a/b/c/welcome")));

    }

    @Test
    public void loadValidatesMetadata() throws IOException {
        endpointLoader.load(C_D_F);
        JsonNode metadata = getJsonTestFile(C_D_F, "metadata.json");
        verify(koValidationService, times(1)).validateMetadata(metadata);
    }

    @Test
    public void loadValidatesServiceSpec() throws IOException {
        endpointLoader.load(C_D_F);
        JsonNode serviceSpec = getYamlTestFile(C_D_F, "service.yaml");
        verify(koValidationService, times(1)).validateServiceSpecification(serviceSpec);
    }


    @Test
    public void loadValidatesObjectForActivation() throws IOException {
        Map<URI, Endpoint> load = endpointLoader.load(C_D_F);
        verify(koValidationService).validateEndpoint(load.get(URI.create("c/d/f/welcome")));
        verify(koValidationService).validateEndpoint(load.get(URI.create("c/d/f/info")));
        verify(koValidationService).validateEndpoint(load.get(URI.create("c/d/f/goodbye")));
    }

    /*
     * Test loader methods
     */
    private void loadMockRepoWithKOs() throws IOException {
        final Map<ArkId, JsonNode> kos = new HashMap<>();
        final KnowledgeObjectWrapper abcKow = new KnowledgeObjectWrapper(getJsonTestFile(A_B_C, "metadata.json"));
        abcKow.addDeployment(getYamlTestFile(A_B_C, "deployment.yaml"));
        abcKow.addService(getYamlTestFile(A_B_C, "service.yaml"));
        final KnowledgeObjectWrapper cdeKow = new KnowledgeObjectWrapper(getJsonTestFile(C_D_E, "metadata.json"));
        cdeKow.addDeployment(getYamlTestFile(C_D_E, "deployment.yaml"));
        cdeKow.addService(getYamlTestFile(C_D_E, "service.yaml"));
        final KnowledgeObjectWrapper cdfKow = new KnowledgeObjectWrapper(getJsonTestFile(C_D_F, "metadata.json"));
        cdfKow.addDeployment(getYamlTestFile(C_D_F, "deployment.yaml"));
        cdfKow.addService(getYamlTestFile(C_D_F, "service.yaml"));
        kos.put(A_B_C, getJsonTestFile(A_B_C, "metadata.json"));
        kos.put(C_D_E, getJsonTestFile(C_D_E, "metadata.json"));
        kos.put(C_D_F, getJsonTestFile(C_D_F, "metadata.json"));
        given(repository.getKow(A_B_C)).willReturn(abcKow);
        given(repository.getKow(C_D_E)).willReturn(cdeKow);
        given(repository.getKow(C_D_F)).willReturn(cdfKow);
        given(repository.findAll()).willReturn(kos);
        given(repository.findKnowledgeObjectMetadata(C_D_E)).willReturn(
                getJsonTestFile(C_D_E, "metadata.json"));
    }

    private void loadMockRepoWithServiceSpecs() throws IOException {
        given(repository.findServiceSpecification(eq(A_B_C), any()))
                .willReturn(getYamlTestFile(A_B_C, "service.yaml"));
        given(repository.findServiceSpecification(eq(C_D_E), any()))
                .willReturn(getYamlTestFile(C_D_E, "service.yaml"));
        given(repository.findServiceSpecification(eq(C_D_F), any()))
                .willReturn(getYamlTestFile(C_D_F, "service.yaml"));
    }

    private void loadMockRepoWithImplementations() throws IOException {
        given(repository.findKnowledgeObjectMetadata(eq(A_B_C)))
                .willReturn(getJsonTestFile(A_B_C, "metadata.json"));
        given(repository.findKnowledgeObjectMetadata(eq(C_D_E)))
                .willReturn(getJsonTestFile(C_D_E, "metadata.json"));
        given(repository.findKnowledgeObjectMetadata(eq(C_D_F)))
                .willReturn(getJsonTestFile(C_D_F, "metadata.json"));

    }

    private void loadMockRepoWithDeploymentSpecs() throws IOException {
        given(repository.findDeploymentSpecification(eq(A_B_C), any()))
                .willReturn(getYamlTestFile(A_B_C, "deployment.yaml"));
        given(repository.findDeploymentSpecification(eq(C_D_E), any()))
                .willReturn(getYamlTestFile(C_D_E, "deployment.yaml"));
        given(repository.findDeploymentSpecification(eq(C_D_F), any()))
                .willReturn(getYamlTestFile(C_D_F, "deployment.yaml"));
    }
}

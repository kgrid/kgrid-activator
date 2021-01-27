package org.kgrid.activator.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kgrid.activator.EndpointLoader;
import org.kgrid.activator.constants.EndpointStatus;
import org.kgrid.activator.domain.Endpoint;
import org.kgrid.activator.exceptions.ActivatorException;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.kgrid.activator.testUtilities.KoCreationTestHelper.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EndpointLoaderTest {

    @Mock
    private KnowledgeObjectRepository repository;

    @Mock
    private KoValidationService koValidationService;

    @InjectMocks
    private EndpointLoader endpointLoader;

    private ArkId versionlessArk;

    private Map<URI, Endpoint> endpointMap;

    private JsonNode koMetadata;

    @BeforeEach
    public void setUp() {
        endpointMap = new TreeMap<>();
        final Endpoint jsEndpoint = getEndpointForEngine(JS_ENGINE);
        endpointMap.put(JS_ENDPOINT_URI, jsEndpoint);
        versionlessArk = new ArkId("naan", "name", null);
        koMetadata = generateMetadata(JS_NAAN, JS_NAME, JS_VERSION);
        doNothing().when(koValidationService).validateMetadata(koMetadata);
        when(repository.getKow(JS_ARK_ID)).thenReturn(getEndpointForEngine(JS_ENGINE).getWrapper());
        Mockito.lenient().when(repository.findKnowledgeObjectMetadata(versionlessArk)).thenReturn(new ObjectMapper().createArrayNode().add(koMetadata));
    }

    @Test
    @DisplayName("Loading an object creates an endpoint map")
    public void loadCreatesNewEndpointMap() {
        Map<URI, Endpoint> expected = new TreeMap<>();
        final Endpoint jsEndpoint = getEndpointForEngine(JS_ENGINE);
        expected.put(JS_ENDPOINT_URI, jsEndpoint);
        Map<URI, Endpoint> eps = endpointLoader.load(JS_ARK_ID);
        assertAll(
                () -> verify(koValidationService).validateServiceSpecification(generateServiceNode(JS_ENGINE)),
                () -> verify(koValidationService).validateEndpoint(any(Endpoint.class)),
                () -> assertEquals(expected.get(JS_ENDPOINT_URI).getExecutor(), eps.get(JS_ENDPOINT_URI).getExecutor())
        );
    }

    @Test
    @DisplayName("Loading an object without a version uses the metadata version")
    public void loadObjectWithoutVersionGetsVersionFromMetadata() {
        Map<URI, Endpoint> eps = endpointLoader.load(versionlessArk);
        assertAll(
                () -> verify(koValidationService).validateServiceSpecification(generateServiceNode(JS_ENGINE)),
                () -> verify(koValidationService).validateEndpoint(any(Endpoint.class)),
                () -> assertEquals(endpointMap.get(JS_ENDPOINT_URI).getExecutor(), eps.get(JS_ENDPOINT_URI).getExecutor())
        );
    }

    @Test
    @DisplayName("Loading an invalid object sets the status correctly")
    public void loadImplementationSetsStatusOnActivatorEx() {
        final String missingDeploy = "Has missing Deployment Specification";
        doThrow(new ActivatorException(missingDeploy)).when(koValidationService).validateEndpoint(any());
        final Endpoint jsEndpoint = getEndpointForEngine(JS_ENGINE);
        jsEndpoint.setDetail(missingDeploy);
        endpointMap.put(JS_ENDPOINT_URI, jsEndpoint);
        Map<URI, Endpoint> eps = endpointLoader.load(versionlessArk);
        assertAll(
                () -> verify(koValidationService).validateServiceSpecification(generateServiceNode(JS_ENGINE)),
                () -> verify(koValidationService).validateEndpoint(any(Endpoint.class)),
                () -> assertNotNull(eps.get(JS_ENDPOINT_URI).getStatus()),
                () -> assertEquals(EndpointStatus.INVALID.name(), eps.get(JS_ENDPOINT_URI).getStatus()),
                () -> assertEquals(endpointMap.get(JS_ENDPOINT_URI).getDetail(), eps.get(JS_ENDPOINT_URI).getDetail())
        );
    }

    @Test
    @DisplayName("Loading all loads all endpoints")
    public void loadsAllEndpoints() {
        Map<ArkId, JsonNode> kos = new HashMap<>();
        kos.put(JS_ARK_ID, koMetadata);
        when(repository.findAll()).thenReturn(kos);
        Map<URI, Endpoint> endpoints = endpointLoader.load();
        assertEquals(endpointMap.get(JS_ENDPOINT_URI).getStatus(), endpoints.get(JS_ENDPOINT_URI).getStatus());
    }
}

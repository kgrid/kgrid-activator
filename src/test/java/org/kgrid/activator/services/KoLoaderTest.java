package org.kgrid.activator.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kgrid.activator.domain.Endpoint;
import org.kgrid.activator.exceptions.ActivatorException;
import org.kgrid.shelf.ShelfResourceNotFound;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.domain.KnowledgeObjectWrapper;
import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.kgrid.activator.testUtilities.KoCreationTestHelper.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Ko Loader Tests")
public class KoLoaderTest {

    @Spy
    Map<URI, Endpoint> endpointMap;

    @Spy
    private KoValidationService koValidationService;

    @Mock
    private KnowledgeObjectRepository knowledgeObjectRepository;

    @InjectMocks
    private KoLoader koLoader;

    private final Map<ArkId, JsonNode> kos = new HashMap<>();
    private final JsonNode jsMetadata = generateMetadata(JS_NAAN, JS_NAME, JS_VERSION);
    private final JsonNode nodeMetadata = generateMetadata(NODE_NAAN, NODE_NAME, NODE_VERSION);
    final KnowledgeObjectWrapper jsWrapper = new KnowledgeObjectWrapper(jsMetadata);
    private final KnowledgeObjectWrapper nodeWrapper = new KnowledgeObjectWrapper(nodeMetadata);
    private Endpoint jsEndpoint;
    private Endpoint nodeEndpoint;

    @BeforeEach
    public void setUp() {
        jsWrapper.addDeployment(getEndpointDeploymentJsonForEngine(JS_ENGINE, JS_ENDPOINT_NAME));
        jsWrapper.addService(generateServiceNode(JS_ENGINE));
        jsEndpoint = new Endpoint(jsWrapper, JS_ENDPOINT_NAME);
        nodeWrapper.addDeployment(getEndpointDeploymentJsonForEngine(NODE_ENGINE, NODE_ENDPOINT_NAME));
        nodeWrapper.addService(generateServiceNode(NODE_ENGINE));
        nodeEndpoint = new Endpoint(nodeWrapper, NODE_ENDPOINT_NAME);
        kos.put(JS_ARK_ID, jsMetadata);
        kos.put(NODE_ARK_ID, nodeMetadata);
        when(knowledgeObjectRepository.getKow(JS_ARK_ID)).thenReturn(jsWrapper);
    }

    @Test
    public void loadAllLoadsEveryKnowledgeObjectOnShelf() {
        when(knowledgeObjectRepository.findAll()).thenReturn(kos);
        when(knowledgeObjectRepository.getKow(NODE_ARK_ID)).thenReturn(nodeWrapper);
        Map<URI, Endpoint> loadedEndpoints = koLoader.loadAllKos();
        Map<URI, Endpoint> expected = new HashMap<>();

        expected.put(jsEndpoint.getId(), jsEndpoint);
        expected.put(nodeEndpoint.getId(), nodeEndpoint);
        assertAll(
                () -> verify(knowledgeObjectRepository).getKow(JS_ARK_ID),
                () -> verify(knowledgeObjectRepository).getKow(NODE_ARK_ID),
                () -> assertEquals(expected, loadedEndpoints)
        );
    }

    @Test
    public void loadSomeReturnsEndpoints() {
        when(knowledgeObjectRepository.getKow(NODE_ARK_ID)).thenReturn(nodeWrapper);
        List<ArkId> arkIds = new ArrayList<>();
        arkIds.add(JS_ARK_ID);
        arkIds.add(NODE_ARK_ID);
        Map<URI, Endpoint> loadedEndpoints = koLoader.loadSomeKos(arkIds);
        Map<URI, Endpoint> expected = new HashMap<>();

        expected.put(jsEndpoint.getId(), jsEndpoint);
        expected.put(nodeEndpoint.getId(), nodeEndpoint);
        assertAll(
                () -> verify(knowledgeObjectRepository).getKow(JS_ARK_ID),
                () -> verify(knowledgeObjectRepository).getKow(NODE_ARK_ID),
                () -> assertEquals(expected, loadedEndpoints)
        );

    }

    @Test
    public void loadOneReturnsEndpoints() {
        Map<URI, Endpoint> loadedEndpoints = koLoader.loadOneKo(JS_ARK_ID);
        Map<URI, Endpoint> expected = new HashMap<>();

        expected.put(jsEndpoint.getId(), jsEndpoint);
        assertAll(
                () -> verify(koValidationService).validateKow(jsWrapper),
                () -> verify(koValidationService).validateEndpoint(jsEndpoint),
                () -> verify(endpointMap).putAll(loadedEndpoints),
                () -> assertEquals(expected, loadedEndpoints)
        );
    }

    @Test
    public void endpointFailsWrapperValidationThrowsError() {
        KnowledgeObjectWrapper badWrapper = new KnowledgeObjectWrapper(jsMetadata);
        when(knowledgeObjectRepository.getKow(JS_ARK_ID)).thenReturn(badWrapper);

        Exception ex = assertThrows(ActivatorException.class, () -> koLoader.loadOneKo(JS_ARK_ID));
        assertEquals(String.format("Cannot load ko %s, Has missing paths node in Service Specification", JS_ARK_ID.getFullArk()), ex.getMessage());
        assertEquals(ActivatorException.class, ex.getCause().getClass());
    }

    @Test
    public void endpointFailsEndpointValidationThrowsError() throws JsonProcessingException {
        KnowledgeObjectWrapper dubiousWrapper = new KnowledgeObjectWrapper(jsMetadata);
        final ObjectMapper objectMapper = new ObjectMapper();
        JsonNode deploymentSpec =
                objectMapper.readTree(
                        "{\"/endpoint\":{\"post\":{\"artifact\":\"Arty McFacts\",\"engine\":\"javascript\",\"function\":\"doorway\"}}}");
        JsonNode mismatchServiceSpec = objectMapper.readTree("{\"paths\":{\"/MISMATCH\":{\"post\":{\"stuff\":\"things\"}}}}");
        dubiousWrapper.addDeployment(deploymentSpec);
        dubiousWrapper.addService(mismatchServiceSpec);
        when(knowledgeObjectRepository.getKow(JS_ARK_ID)).thenReturn(dubiousWrapper);

        Exception ex = assertThrows(ActivatorException.class, () -> koLoader.loadOneKo(JS_ARK_ID));
        verify(koValidationService).validateEndpoint(new Endpoint(dubiousWrapper, JS_ENDPOINT_NAME));
        assertEquals(String.format("Cannot load ko %s, Has missing endpoint path in Service Specification that is listed in Deployment Specification", JS_ARK_ID.getFullArk()), ex.getMessage());
        assertEquals(ActivatorException.class, ex.getCause().getClass());
    }

    @Test
    public void endpointShelfFetchThrowsError() {
        when(knowledgeObjectRepository.getKow(JS_ARK_ID)).thenThrow(new ShelfResourceNotFound("Object location not found for ark id " + JS_ARK_ID.getFullArk()));
        Exception ex = assertThrows(ActivatorException.class, () -> koLoader.loadOneKo(JS_ARK_ID));
        assertEquals(String.format("Cannot load ko %s, Object location not found for ark id %s", JS_ARK_ID.getFullArk(), JS_ARK_ID.getFullArk()), ex.getMessage());
        assertEquals(ShelfResourceNotFound.class, ex.getCause().getClass());
    }
}
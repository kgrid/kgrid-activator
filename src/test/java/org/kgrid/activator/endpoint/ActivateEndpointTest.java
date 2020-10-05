package org.kgrid.activator.endpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kgrid.activator.EndpointLoader;
import org.kgrid.activator.services.ActivationService;
import org.kgrid.activator.services.Endpoint;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.domain.KnowledgeObjectWrapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.kgrid.activator.utils.KoCreationTestHelper.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ActivateEndpointTest {

    @Mock
    private ActivationService activationService;
    @Mock
    private EndpointLoader endpointLoader;
    @Mock
    private Map<URI, Endpoint> globalEndpointMap;
    @InjectMocks
    ActivateEndpoint activateEndpoint;

    Map<URI, Endpoint> endpointMap = new HashMap<>();
    private ObjectMapper objectMapper = new ObjectMapper();
    private KnowledgeObjectWrapper
            kow1 = new KnowledgeObjectWrapper(generateMetadata());
    private Endpoint
            endpoint1 = new Endpoint(kow1, ENDPOINT_NAME);

    @Before
    public void setup() {
        kow1.addService(generateServiceNode());
        kow1.addDeployment(getEndpointDeploymentJson());
        endpointMap.put(URI.create(String.format("%s/%s/%s/%s", NAAN, NAME, VERSION, ENDPOINT_NAME)), endpoint1);
        when(endpointLoader.load()).thenReturn(endpointMap);
        when(endpointLoader.load(endpoint1.getArkId())).thenReturn(endpointMap);
        when(endpointLoader.load(new ArkId(NAAN, NAME))).thenReturn(endpointMap);
        when(globalEndpointMap.values()).thenReturn(endpointMap.values());
    }

    @Test
    public void activate_LoadsAllEndpoints() {
        activateEndpoint.activate();
        verify(endpointLoader).load();
    }

    @Test
    public void activate_LoadsAllEndpointsIntoEndpointMap() {
        activateEndpoint.activate();
        verify(globalEndpointMap).putAll(endpointMap);
    }

    @Test
    public void activate_ActivatesEndpointMap() {
        activateEndpoint.activate();
        verify(activationService).activate(globalEndpointMap);
    }

    @Test
    public void activate_ReturnsActivationResults() throws JsonProcessingException {
        String results = activateEndpoint.activate();
        checkActivationResults(results);
    }

    @Test
    public void activateForEngine_loadsEndpointsWithGivenEngine() {
        activateEndpoint.activateForEngine(ENGINE);
        verify(endpointLoader).load(endpoint1.getArkId());
    }

    @Test
    public void activateForEngine_activatesEndpointsWithGivenEngine() {
        activateEndpoint.activateForEngine(ENGINE);
        verify(activationService).activate(endpointMap);
    }

    @Test
    public void activateForEngine_addsActivatedEndpointsToGlobalMap() {
        activateEndpoint.activateForEngine(ENGINE);
        verify(globalEndpointMap).putAll(endpointMap);
    }

    @Test
    public void activateForEngine_ReturnsActivationResults() throws JsonProcessingException {
        String results = activateEndpoint.activateForEngine(ENGINE);
        checkActivationResults(results);
    }

    @Test
    public void activateKo_loadsEndpointsWithGivenNaanAndName() {
        activateEndpoint.activateKo(NAAN, NAME);
        verify(endpointLoader).load(new ArkId(NAAN, NAME));
    }

    @Test
    public void activateKo_activatesEndpointsWithGivenNaanAndName() {
        activateEndpoint.activateKo(NAAN, NAME);
        verify(activationService).activate(endpointMap);
    }

    @Test
    public void activateKo_ReturnsActivationResults() throws JsonProcessingException {
        String results = activateEndpoint.activateKo(NAAN, NAME);
        checkActivationResults(results);
    }

    @Test
    public void activateKoVersion_loadsEndpointsWithGivenNaanAndNameAndVersion() {
        activateEndpoint.activateKoVersion(NAAN, NAME, VERSION);
        verify(endpointLoader).load(new ArkId(NAAN, NAME, VERSION));
    }

    @Test
    public void activateKoVersion_activatesEndpointsWithGivenNaanAndName() {
        activateEndpoint.activateKoVersion(NAAN, NAME, VERSION);
        verify(activationService).activate(endpointMap);
    }

    @Test
    public void activateKoVersion_ReturnsActivationResults() throws JsonProcessingException {
        String results = activateEndpoint.activateKoVersion(NAAN, NAME, VERSION);
        checkActivationResults(results);
    }

    private void checkActivationResults(String results) throws JsonProcessingException {
        JsonNode epResultNode = objectMapper.readTree(results).get(0);
        assertEquals(String.format("/%s/%s/%s%s?v=%s", NAAN, NAME, VERSION, ENDPOINT_NAME, API_VERSION),
                epResultNode.get("path").asText());
        assertNotNull(epResultNode.get("activated"));
    }
}
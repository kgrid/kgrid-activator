package org.kgrid.activator.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kgrid.activator.domain.Endpoint;
import org.kgrid.activator.services.ActivationService;
import org.kgrid.activator.services.KoLoader;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.kgrid.activator.testUtilities.KoCreationTestHelper.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Activation Controller Tests")
public class ActivationControllerTest {
    @Mock
    private ActivationService activationService;

    @Mock
    private KoLoader koLoader;

    @Mock
    private Map<URI, Endpoint> globalEndpoints;

    @InjectMocks
    ActivationController activationController;

    private final Map<URI, Endpoint> endpointMapFromLoader = new HashMap<>();
    private final Map<URI, Endpoint> justNodeEndpoints = new HashMap<>();
    ArrayList<Endpoint> globalEndpointList = new ArrayList<>();

    @BeforeEach
    public void setup() {
        Endpoint jsEndpoint = getEndpointForEngine(JS_ENGINE);
        URI jsEndpointUri = URI.create(JS_ENDPOINT_NAME);

        Endpoint nodeEndpoint = getEndpointForEngine(NODE_ENGINE);
        URI nodeEndpointUri = URI.create(NODE_ENDPOINT_NAME);

        endpointMapFromLoader.put(jsEndpointUri, jsEndpoint);
        endpointMapFromLoader.put(nodeEndpointUri, nodeEndpoint);
        justNodeEndpoints.put(nodeEndpoint.getId(), nodeEndpoint);
        globalEndpointList.add(jsEndpoint);
        globalEndpointList.add(nodeEndpoint);
    }

//    @Test
//    @DisplayName("Global activation interactions")
//    public void testActivateInteractionsAndResult() {
//        when(koLoader.loadAllKos()).thenReturn(endpointMapFromLoader);
//        RedirectView redirectView = activationController.activate();
//        assertAll(
//                () -> verify(globalEndpoints).clear(),
//                () -> verify(koLoader).loadAllKos(),
//                () -> verify(globalEndpoints).putAll(endpointMapFromLoader),
//                () -> verify(activationService).activateEndpoints(globalEndpoints),
//                () -> assertEquals("/endpoints", redirectView.getUrl())
//        );
//    }

    @Test
    @DisplayName("Engine activation interactions")
    public void testActivateForEngineInteractionsAndResult() {
        RedirectView redirectView = activationController.activateForEngine(NODE_ENGINE);
        assertEquals("/endpoints/" + NODE_ENGINE, redirectView.getUrl());
    }

    @Test
    @DisplayName("Single ko activation interactions")
    public void testActivateKoInteractionsAndResult() {
        RedirectView redirectView = activationController.activateKo(NODE_NAAN, NODE_NAME);
    }

    @Test
    @DisplayName("Single ko version activation interactions")
    public void testActivateKoVersionInteractionsAndResult() {
        RedirectView redirectView = activationController.activateKoVersion(NODE_NAAN, NODE_NAME, JS_API_VERSION);

    }
}

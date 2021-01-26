package org.kgrid.activator.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kgrid.activator.EndpointLoader;
import org.kgrid.activator.services.ActivationService;
import org.kgrid.activator.services.Endpoint;
import org.kgrid.shelf.domain.ArkId;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.kgrid.activator.testUtilities.KoCreationTestHelper.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Activation Controller Tests")
public class ActivationControllerTest {
    @Mock
    private ActivationService activationService;

    @Mock
    private EndpointLoader endpointLoader;

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

    @Test
    @DisplayName("Global activation interactions")
    public void testActivateInteractionsAndResult() {
        when(endpointLoader.load()).thenReturn(endpointMapFromLoader);
        RedirectView redirectView = activationController.activate();
        assertAll(
                () -> verify(globalEndpoints).clear(),
                () -> verify(endpointLoader).load(),
                () -> verify(globalEndpoints).putAll(endpointMapFromLoader),
                () -> verify(activationService).activateEndpoints(globalEndpoints),
                () -> assertEquals("/endpoints", redirectView.getUrl())
        );
    }

    @Test
    @DisplayName("Engine activation interactions")
    public void testActivateForEngineInteractionsAndResult() {
        when(globalEndpoints.values()).thenReturn(globalEndpointList);
        RedirectView redirectView = activationController.activateForEngine(NODE_ENGINE);
        assertAll(
                () -> verify(activationService).activateEndpoints(justNodeEndpoints),
                () -> verify(globalEndpoints).putAll(justNodeEndpoints),
                () -> assertEquals("/endpoints/" + NODE_ENGINE, redirectView.getUrl())
        );
    }

    @Test
    @DisplayName("Single ko activation interactions")
    public void testActivateKoInteractionsAndResult() {
        when(endpointLoader.load(new ArkId(NODE_NAAN, NODE_NAME))).thenReturn(justNodeEndpoints);
        RedirectView redirectView = activationController.activateKo(NODE_NAAN, NODE_NAME);
        assertAll(
                () -> verify(endpointLoader).load(new ArkId(NODE_NAAN, NODE_NAME)),
                () -> verify(globalEndpoints).putAll(justNodeEndpoints),
                () -> assertEquals("/endpoints", redirectView.getUrl())
        );
    }

    @Test
    @DisplayName("Single ko version activation interactions")
    public void testActivateKoVersionInteractionsAndResult() {
        when(endpointLoader.load(new ArkId(NODE_NAAN, NODE_NAME, JS_API_VERSION))).thenReturn(justNodeEndpoints);
        RedirectView redirectView = activationController.activateKoVersion(NODE_NAAN, NODE_NAME, JS_API_VERSION);
        assertAll(
                () -> verify(endpointLoader).load(new ArkId(NODE_NAAN, NODE_NAME, JS_API_VERSION)),
                () -> verify(activationService).activateEndpoints(justNodeEndpoints),
                () -> verify(globalEndpoints).putAll(justNodeEndpoints),
                () -> assertEquals("/endpoints", redirectView.getUrl())
        );
    }
}

package org.kgrid.activator.controller;

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
import org.springframework.web.servlet.view.RedirectView;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.kgrid.activator.utils.KoCreationTestHelper.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ActivationControllerTest {
    public static final String NODE_ENDPOINT_NAME = "/node-endpoint";
    public static final String NODE_NAAN = "node-naan";
    public static final String NODE_NAME = "node-name";
    public static final String NODE_VERSION = "node-version";
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
    ArrayList endpointList = new ArrayList();

    @Before
    public void setup() {
        KnowledgeObjectWrapper jsKow = new KnowledgeObjectWrapper(generateMetadata(NAAN, NAME, VERSION));
        jsKow.addService(generateServiceNode());
        jsKow.addDeployment(getEndpointDeploymentJsonForEngine(JS_ENGINE, ENDPOINT_NAME));
        KnowledgeObjectWrapper nodeKow = new KnowledgeObjectWrapper(generateMetadata(NODE_NAAN, NODE_NAME, NODE_VERSION));
        nodeKow.addService(generateServiceNode());
        nodeKow.addDeployment(getEndpointDeploymentJsonForEngine(NODE_ENGINE, NODE_ENDPOINT_NAME));
        Endpoint jsEndpoint = new Endpoint(jsKow, ENDPOINT_NAME);
        Endpoint nodeEndpoint = new Endpoint(nodeKow, NODE_ENDPOINT_NAME);
        URI jsEndpointUri = URI.create(ENDPOINT_NAME);
        URI nodeEndpointUri = URI.create(NODE_ENDPOINT_NAME);
        endpointMapFromLoader.put(jsEndpointUri, jsEndpoint);
        endpointMapFromLoader.put(nodeEndpointUri, nodeEndpoint);
        justNodeEndpoints.put(nodeEndpoint.getId(), nodeEndpoint);
        endpointList.add(jsEndpoint);
        endpointList.add(nodeEndpoint);
        when(globalEndpoints.values()).thenReturn(endpointList);
        when(endpointLoader.load()).thenReturn(endpointMapFromLoader);
        when(endpointLoader.load(new ArkId(NODE_NAAN, NODE_NAME))).thenReturn(justNodeEndpoints);
        when(endpointLoader.load(new ArkId(NODE_NAAN, NODE_NAME, API_VERSION))).thenReturn(justNodeEndpoints);
    }

    @Test
    public void testActivateInteractions() {
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
    public void testActivateForEngineActivatesEngineEndpointsAndPutsThemInMap() {
        RedirectView redirectView = activationController.activateForEngine(NODE_ENGINE);
        assertAll(
                () -> verify(activationService).activateEndpoints(justNodeEndpoints),
                () -> verify(globalEndpoints).putAll(justNodeEndpoints),
                () -> assertEquals("/endpoints/" + NODE_ENGINE, redirectView.getUrl())
        );
    }

    @Test
    public void testActivateKoLoadsOnlyKoEndpoints() {
        RedirectView redirectView = activationController.activateKo(NODE_NAAN, NODE_NAME);
        assertAll(
                () -> verify(endpointLoader).load(new ArkId(NODE_NAAN, NODE_NAME)),
                () -> verify(globalEndpoints).putAll(justNodeEndpoints),
                () -> assertEquals("/endpoints", redirectView.getUrl())
        );
    }

    @Test
    public void testActivateKoVersionPutsKoEndpointsInGlobalMap() {
        RedirectView redirectView = activationController.activateKoVersion(NODE_NAAN, NODE_NAME, API_VERSION);
        assertAll(
                () -> verify(endpointLoader).load(new ArkId(NODE_NAAN, NODE_NAME, API_VERSION)),
                () -> verify(activationService).activateEndpoints(justNodeEndpoints),
                () -> verify(globalEndpoints).putAll(justNodeEndpoints),
                ()-> assertEquals("/endpoints",redirectView.getUrl())
        );
    }
}

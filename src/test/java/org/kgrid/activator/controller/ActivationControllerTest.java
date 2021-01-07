package org.kgrid.activator.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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

    private Map<URI, Endpoint> endpointMapFromLoader = new HashMap<>();
    private Endpoint nodeEndpoint;
    private Map<URI, Endpoint> justNodeEndpoints = new HashMap<>();
    ArrayList endpointList = new ArrayList();
    JsonArray activationResults = new JsonArray();
    private KnowledgeObjectWrapper jsKow;
    private KnowledgeObjectWrapper nodeKow;
    private Endpoint jsEndpoint;

    @Before
    public void setup() {
        jsKow = new KnowledgeObjectWrapper(generateMetadata(NAAN, NAME, VERSION));
        jsKow.addService(generateServiceNode());
        jsKow.addDeployment(getEndpointDeploymentJsonForEngine(JS_ENGINE, ENDPOINT_NAME));
        nodeKow = new KnowledgeObjectWrapper(generateMetadata(NODE_NAAN, NODE_NAME, NODE_VERSION));
        nodeKow.addService(generateServiceNode());
        nodeKow.addDeployment(getEndpointDeploymentJsonForEngine(NODE_ENGINE, NODE_ENDPOINT_NAME));
        jsEndpoint = new Endpoint(jsKow, ENDPOINT_NAME);
        nodeEndpoint = new Endpoint(nodeKow, NODE_ENDPOINT_NAME);
        URI jsEndpointUri = URI.create(ENDPOINT_NAME);
        URI nodeEndpointUri = URI.create(NODE_ENDPOINT_NAME);
        endpointMapFromLoader.put(jsEndpointUri, jsEndpoint);
        endpointMapFromLoader.put(nodeEndpointUri, nodeEndpoint);

        justNodeEndpoints.put(nodeEndpoint.getId(), nodeEndpoint);
        activationResults = getActivationResults(endpointMapFromLoader);

        endpointList.add(jsEndpoint);
        endpointList.add(nodeEndpoint);
        when(globalEndpoints.values()).thenReturn(endpointList);
        when(endpointLoader.load()).thenReturn(endpointMapFromLoader);
        when(endpointLoader.load(new ArkId(NODE_NAAN, NODE_NAME))).thenReturn(justNodeEndpoints);
        when(endpointLoader.load(new ArkId(NODE_NAAN, NODE_NAME, API_VERSION))).thenReturn(justNodeEndpoints);
    }

    @Test
    public void testActivateClearsEndpointList() {
        activationController.activate();
        verify(globalEndpoints).clear();
    }

    @Test
    public void testActivateLoadsEndpoints() {
        activationController.activate();
        verify(endpointLoader).load();
    }

    @Test
    public void testActivateAddsLoadedEndpointsToGlobalMap() {
        RedirectView redirectView = activationController.activate();
        verify(globalEndpoints).putAll(endpointMapFromLoader);
        assertEquals("/endpoints", redirectView.getUrl());
    }

    @Test
    public void testActivateRedirectsToEndpoints() {
        RedirectView redirectView = activationController.activate();
        assertEquals("/endpoints", redirectView.getUrl());
    }

    @Test
    public void testActivateForEngineRedirectsToEndpointsForEngine() {
        RedirectView redirectView = activationController.activateForEngine("javascript");
        assertEquals("/endpoints/javascript", redirectView.getUrl());
    }

    @Test
    public void testActivateActivatesLoadedEndpoints() {
        activationController.activate();
        verify(activationService).activate(globalEndpoints);
    }

    @Test
    public void testRefreshClearsEndpointList() {
        activationController.activate();
        verify(globalEndpoints).clear();
    }

    @Test
    public void testRefreshLoadsEndpoints() {
        activationController.activate();
        verify(endpointLoader).load();
    }

    @Test
    public void testRefreshAddsLoadedEndpointsToGlobalMap() {
        activationController.activate();
        verify(globalEndpoints).putAll(endpointMapFromLoader);
    }

    @Test
    public void testRefreshActivatesLoadedEndpoints() {
        activationController.activate();
        verify(activationService).activate(globalEndpoints);
    }

    @Test
    public void testActivateForEngineActivatesOnlyEngineExndpoints() {
        activationController.activateForEngine(NODE_ENGINE);
        verify(activationService).activate(justNodeEndpoints);
    }

    @Test
    public void testActivateForEnginePutsOnlyEngineEndpointsInGlobalMap() {
        activationController.activateForEngine(NODE_ENGINE);
        verify(globalEndpoints).putAll(justNodeEndpoints);
    }

    @Test
    public void testActivateKoLoadsOnlyKoEndpoints() {
        activationController.activateKo(NAAN, NAME);
        verify(endpointLoader).load(new ArkId(NAAN, NAME));
    }

    @Test
    public void testActivateKoPutsKoEndpointsInGlobalMap() {
        activationController.activateKo(NODE_NAAN, NODE_NAME);
        verify(globalEndpoints).putAll(justNodeEndpoints);
    }

    @Test
    public void testActivateKoVersionLoadsOnlyKoEndpoints() {
        activationController.activateKoVersion(NAAN, NAME, API_VERSION);
        verify(endpointLoader).load(new ArkId(NAAN, NAME, API_VERSION));
    }

    @Test
    public void testActivateKoVersionPutsKoEndpointsInGlobalMap() {
        activationController.activateKoVersion(NODE_NAAN, NODE_NAME, API_VERSION);
        verify(globalEndpoints).putAll(justNodeEndpoints);
    }

    private JsonArray getActivationResults(Map<URI, Endpoint> endpoints) {
        JsonArray endpointActivations = new JsonArray();

        endpoints.values().forEach(endpoint -> {
            JsonObject endpointActivationResult = new JsonObject();
            endpointActivationResult.addProperty("@id", "/" + endpoint.getId());
            endpointActivationResult.addProperty("activated", endpoint.getActivated().toString());
            endpointActivationResult.addProperty("status", endpoint.getStatus());
            endpointActivations.add(endpointActivationResult);
        });
        return endpointActivations;
    }
}

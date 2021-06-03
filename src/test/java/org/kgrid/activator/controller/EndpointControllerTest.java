package org.kgrid.activator.controller;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kgrid.activator.domain.Endpoint;
import org.kgrid.activator.exceptions.ActivatorException;
import org.kgrid.activator.services.ActivationService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.kgrid.activator.testUtilities.KoCreationTestHelper.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Endpoint Controller Tests")
public class EndpointControllerTest {

    @Mock
    private ActivationService activationService;


    @InjectMocks
    EndpointController endpointController;
    private Endpoint jsEndpoint;
    private Endpoint nodeEndpoint;
    private final Map<URI, Endpoint> endpointMap = new TreeMap<>();

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(endpointController, "shelfRoot", "kos");
        jsEndpoint = getEndpointForEngine(JS_ENGINE);
        nodeEndpoint = getEndpointForEngine(NODE_ENGINE);
        endpointMap.put(JS_ENDPOINT_URI, jsEndpoint);
        endpointMap.put(NODE_ENDPOINT_URI, nodeEndpoint);
    }

    @Test
    @DisplayName("Find all Endpoints returns everything in the global endpoint map")
    public void testFindAllEndpoints() {
        when(activationService.getEndpoints()).thenReturn(endpointMap.values());
        List<EndpointResource> allEndpoints = endpointController.findAllEndpoints();
        assertAll(
                () -> assertTrue(allEndpoints.contains(createEndpointResource(nodeEndpoint))),
                () -> assertTrue(allEndpoints.contains(createEndpointResource(jsEndpoint)))
        );
    }

    @Test
    @DisplayName("Find endpoints for engine returns only that engine's endpoints")
    public void testFindEndpointsForEngine() {
        when(activationService.getEndpoints()).thenReturn(endpointMap.values());
        List<EndpointResource> jsEndpoints = endpointController.findEndpointsForEngine(JS_ENGINE);
        EndpointResource endpointResource = jsEndpoints.get(0);
        assertEquals(JS_ENDPOINT_ID, endpointResource.getId());
    }

    @Test
    @DisplayName("Find endpoints Path Version returns appropriate endpoint resource")
    public void testFindEndpointPathVersionReturnsEndpointResource() {
        when(activationService.createEndpointId(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME)).thenReturn(JS_ENDPOINT_URI);
        when(activationService.getEndpoint(JS_ENDPOINT_URI)).thenReturn(jsEndpoint);
        EndpointResource endpointResource =
                endpointController.findEndpointPathVersion(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME);
        assertEquals(createEndpointResource(jsEndpoint), endpointResource);
    }

    @Test
    @DisplayName("Find endpoints Path Version throws activator exception if no endpoint found")
    public void testFindEndpointPathVersionThrowsActivatorExceptionIfNoEndpointFound() {
        when(activationService.createEndpointId(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME)).thenReturn(JS_ENDPOINT_URI);
//        when(activationService.getEndpoints()).thenReturn((new TreeMap<URI, Endpoint>()).values());
        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> endpointController.findEndpointPathVersion(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME));
        assertEquals(String.format("Cannot find endpoint with id %s/%s/%s/%s", JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME),
                activatorException.getMessage());
    }

    @Test
    @DisplayName("Find endpoints Query Version returns appropriate endpoint resource")
    public void testFindEndpointQueryVersionReturnsEndpointResource() {
        when(activationService.createEndpointId(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME)).thenReturn(JS_ENDPOINT_URI);
        when(activationService.getEndpoint(JS_ENDPOINT_URI)).thenReturn(jsEndpoint);
        List<EndpointResource> endpointResources = endpointController.findEndpointQueryVersion(JS_NAAN, JS_NAME, JS_ENDPOINT_NAME, JS_API_VERSION);
        assertEquals(createEndpointResource(jsEndpoint).getId(), endpointResources.get(0).getId());
    }

    @Test
    @DisplayName("Find endpoints Query Version returns endpoint when version is null")
    public void testFindEndpointQueryVersionReturnsEndpointResource_NullVersion() {
        when(activationService.getAllVersions(JS_NAAN, JS_NAME, JS_ENDPOINT_NAME)).thenReturn(Collections.singletonList(jsEndpoint));
        List<EndpointResource> endpointResources = endpointController.findEndpointQueryVersion(JS_NAAN, JS_NAME, JS_ENDPOINT_NAME, null);
        assertAll(
                () -> verify(activationService).getAllVersions(JS_NAAN, JS_NAME, JS_ENDPOINT_NAME),
                () -> assertEquals(createEndpointResource(jsEndpoint).getId(), endpointResources.get(0).getId())
        );
    }

    @Test
    @DisplayName("Find endpoints Query Version throws activator exception if no endpoint found")
    public void testFindEndpointQueryVersionThrowsActivatorExceptionIfNoEndpointFound() {
        when(activationService.createEndpointId(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME)).thenReturn(JS_ENDPOINT_URI);
        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> endpointController.findEndpointQueryVersion(JS_NAAN, JS_NAME, JS_ENDPOINT_NAME, JS_API_VERSION));
        assertEquals(String.format("Cannot find endpoint with id %s/%s/%s/%s", JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME),
                activatorException.getMessage());
    }

    private EndpointResource createEndpointResource(Endpoint endpoint) {
        return new EndpointResource(endpoint, "kos");
    }
}

package org.kgrid.activator.controller;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kgrid.activator.utilities.EndpointHelper;
import org.kgrid.activator.exceptions.ActivatorException;
import org.kgrid.activator.domain.Endpoint;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.kgrid.activator.testUtilities.KoCreationTestHelper.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Endpoint Controller Tests")
public class EndpointControllerTest {
    @Mock
    private EndpointHelper endpointHelper;
    @Mock
    private Map<URI, Endpoint> endpoints;
    @InjectMocks
    EndpointController endpointController;
    private Endpoint jsEndpoint;
    private Endpoint nodeEndpoint;
    private final HashSet<Map.Entry<URI, Endpoint>> entrySet = new HashSet<>();

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(endpointController, "shelfRoot", "kos");
        jsEndpoint = getEndpointForEngine(JS_ENGINE);
        nodeEndpoint = getEndpointForEngine(NODE_ENGINE);
        entrySet.add(new AbstractMap.SimpleEntry<>(JS_ENDPOINT_URI, jsEndpoint));
        entrySet.add(new AbstractMap.SimpleEntry<>(NODE_ENDPOINT_URI, nodeEndpoint));
    }

    @Test
    @DisplayName("Find all Endpoints returns everything in the global endpoint map")
    public void testFindAllEndpoints() {
        when(endpoints.entrySet()).thenReturn(entrySet);
        List<EndpointResource> allEndpoints = endpointController.findAllEndpoints();
        assertAll(
                () -> assertTrue(allEndpoints.contains(createEndpointResource(nodeEndpoint))),
                () -> assertTrue(allEndpoints.contains(createEndpointResource(jsEndpoint)))
        );
    }

    @Test
    @DisplayName("Find endpoints for engine returns only that engine's endpoints")
    public void testFindEndpointsForEngine() {
        when(endpoints.entrySet()).thenReturn(entrySet);
        List<EndpointResource> jsEndpoints = endpointController.findEndpointsForEngine(JS_ENGINE);
        EndpointResource endpointResource = jsEndpoints.get(0);
        assertEquals(JS_ENDPOINT_ID, endpointResource.getId());
    }

    @Test
    @DisplayName("Find endpoints Path Version returns appropriate endpoint resource")
    public void testFindEndpointPathVersionReturnsEndpointResource() {
        when(endpointHelper.createEndpointId(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME)).thenReturn(JS_ENDPOINT_URI);
        when(endpoints.get(jsEndpoint.getId())).thenReturn(jsEndpoint);
        EndpointResource endpointResource =
                endpointController.findEndpointPathVersion(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME);
        assertEquals(createEndpointResource(jsEndpoint), endpointResource);
    }

    @Test
    @DisplayName("Find endpoints Path Version throws activator exception if no endpoint found")
    public void testFindEndpointPathVersionThrowsActivatorExceptionIfNoEndpointFound() {
        when(endpointHelper.createEndpointId(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME)).thenReturn(JS_ENDPOINT_URI);
        when(endpoints.get(jsEndpoint.getId())).thenReturn(null);
        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> endpointController.findEndpointPathVersion(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME));
        assertEquals(String.format("Cannot find endpoint with id %s/%s/%s/%s", JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME),
                activatorException.getMessage());
    }

    @Test
    @DisplayName("Find endpoints Query Version returns appropriate endpoint resource")
    public void testFindEndpointQueryVersionReturnsEndpointResource() {
        when(endpointHelper.createEndpointId(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME)).thenReturn(JS_ENDPOINT_URI);
        when(endpoints.get(jsEndpoint.getId())).thenReturn(jsEndpoint);
        List<EndpointResource> endpointResources = endpointController.findEndpointQueryVersion(JS_NAAN, JS_NAME, JS_ENDPOINT_NAME, JS_API_VERSION);
        assertEquals(createEndpointResource(jsEndpoint).getId(), endpointResources.get(0).getId());
    }

    @Test
    @DisplayName("Find endpoints Query Version returns endpoint when version is null")
    public void testFindEndpointQueryVersionReturnsEndpointResource_NullVersion() {
        when(endpointHelper.getAllVersions(JS_NAAN, JS_NAME, JS_ENDPOINT_NAME)).thenReturn(Collections.singletonList(jsEndpoint));
        List<EndpointResource> endpointResources = endpointController.findEndpointQueryVersion(JS_NAAN, JS_NAME, JS_ENDPOINT_NAME, null);
        assertAll(
                () -> verify(endpointHelper).getAllVersions(JS_NAAN, JS_NAME, JS_ENDPOINT_NAME),
                () -> assertEquals(createEndpointResource(jsEndpoint).getId(), endpointResources.get(0).getId())
        );
    }

    @Test
    @DisplayName("Find endpoints Query Version throws activator exception if no endpoint found")
    public void testFindEndpointQueryVersionThrowsActivatorExceptionIfNoEndpointFound() {
        when(endpointHelper.createEndpointId(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME)).thenReturn(JS_ENDPOINT_URI);
        when(endpoints.get(jsEndpoint.getId())).thenReturn(null);
        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> endpointController.findEndpointQueryVersion(JS_NAAN, JS_NAME, JS_ENDPOINT_NAME, JS_API_VERSION));
        assertEquals(String.format("Cannot find endpoint with id %s/%s/%s/%s", JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME),
                activatorException.getMessage());
    }

    private EndpointResource createEndpointResource(Endpoint endpoint) {
        return new EndpointResource(endpoint, "kos");
    }
}

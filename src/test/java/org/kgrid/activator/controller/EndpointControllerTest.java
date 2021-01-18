package org.kgrid.activator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kgrid.activator.Utilities.EndpointHelper;
import org.kgrid.activator.exceptions.ActivatorException;
import org.kgrid.activator.services.Endpoint;
import org.kgrid.shelf.domain.KnowledgeObjectWrapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.kgrid.activator.utils.KoCreationTestHelper.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EndpointControllerTest {
    @Mock
    private EndpointHelper endpointHelper;
    @Mock
    private Map<URI, Endpoint> endpoints;
    @InjectMocks
    EndpointController endpointController;
    private KnowledgeObjectWrapper kow;
    private Endpoint endpoint;
    private HttpHeaders headers;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(endpointController, "shelfRoot", "kos");
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        kow = new KnowledgeObjectWrapper(generateMetadata(NAAN, NAME, VERSION));
        kow.addService(generateServiceNode());
        kow.addDeployment(getEndpointDeploymentJsonForEngine(JS_ENGINE, ENDPOINT_NAME));
        endpoint = new Endpoint(kow, ENDPOINT_NAME);
        when(endpoints.get(endpoint.getId())).thenReturn(endpoint);
        when(endpointHelper.getAllVersions(NAAN, NAME, ENDPOINT_NAME)).thenReturn(Collections.singletonList(endpoint));
        when(endpointHelper.createEndpointId(NAAN, NAME, API_VERSION, ENDPOINT_NAME)).thenReturn(ENDPOINT_URI);
        HashSet<Map.Entry<URI, Endpoint>> entrySet = new HashSet<>();
        entrySet.add(new AbstractMap.SimpleEntry<>(ENDPOINT_URI, endpoint));
        when(endpoints.entrySet()).thenReturn(entrySet);
    }

    @Test
    public void testFindAllEndpoints() {
        List<EndpointResource> allEndpoints = endpointController.findAllEndpoints();
        EndpointResource endpointResource = allEndpoints.get(0);
        assertEquals(ENDPOINT_ID, endpointResource.getId());
    }

    @Test
    public void testFindEndpointsForEngine() {
        List<EndpointResource> allEndpoints = endpointController.findEndpointsForEngine(JS_ENGINE);
        EndpointResource endpointResource = allEndpoints.get(0);
        assertEquals(ENDPOINT_ID, endpointResource.getId());
    }

    @Test
    public void testFindEndpointOldVersionReturnsEndpointResource() {
        EndpointResource endpointResource =
                endpointController.findEndpointOldVersion(NAAN, NAME, API_VERSION, ENDPOINT_NAME);
        assertEquals(createEndpointResource(endpoint), endpointResource);
    }

    @Test
    public void testFindEndpointOldVersionThrowsActivatorExceptionIfNoEndpointFound() {
        when(endpoints.get(endpoint.getId())).thenReturn(null);
        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> {
                    endpointController.findEndpointOldVersion(NAAN, NAME, API_VERSION, ENDPOINT_NAME);
                });
        assertEquals(String.format("Cannot find endpoint with id %s/%s/%s/%s", NAAN, NAME, API_VERSION, ENDPOINT_NAME),
                activatorException.getMessage());
    }

    @Test
    public void testFindEndpointReturnsEndpointResource() {
        List<EndpointResource> endpointResources = endpointController.findEndpoint(NAAN, NAME, ENDPOINT_NAME, API_VERSION);
        assertEquals(createEndpointResource(endpoint), endpointResources.get(0));
    }

    @Test
    public void testFindEndpointReturnsEndpointResource_NullVersion() {
        List<EndpointResource> endpointResources = endpointController.findEndpoint(NAAN, NAME, ENDPOINT_NAME, null);
        verify(endpointHelper).getAllVersions(NAAN, NAME, ENDPOINT_NAME);
        assertEquals(createEndpointResource(endpoint), endpointResources.get(0));
    }

    @Test
    public void testFindEndpointReturnsEndpointResourceWithErrorStatusForBadEndpoint() {
        ObjectNode dumbNode = new ObjectMapper().createObjectNode();
        dumbNode.put("trash", "also trash");
        kow.addService(dumbNode);
        kow.addDeployment(dumbNode);
        endpoint = new Endpoint(kow, ENDPOINT_NAME);
        endpoints.replace(endpoint.getId(), endpoint);
        List<EndpointResource> endpointResources = endpointController.findEndpoint(NAAN, NAME, ENDPOINT_NAME, API_VERSION);
        assertEquals(String.format("Could not create endpoint resource for malformed endpoint: %s/%s/%s",
                NAAN, NAME, VERSION), endpointResources.get(0).getStatus());
    }

    @Test
    public void testFindEndpointOldVersionReturnsEndpointResourceWithErrorStatusForBadEndpoint() {
        ObjectNode dumbNode = new ObjectMapper().createObjectNode();
        dumbNode.put("trash", "also trash");
        kow.addService(dumbNode);
        kow.addDeployment(dumbNode);
        endpoint = new Endpoint(kow, ENDPOINT_NAME);
        endpoints.replace(endpoint.getId(), endpoint);
        EndpointResource endpointResource =
                endpointController.findEndpointOldVersion(NAAN, NAME, API_VERSION, ENDPOINT_NAME);
        assertEquals(String.format("Could not create endpoint resource for malformed endpoint: %s/%s/%s",
                NAAN, NAME, VERSION), endpointResource.getStatus());
    }

    @Test
    public void testFindEndpointThrowsActivatorExceptionIfNoEndpointFound() {
        when(endpoints.get(endpoint.getId())).thenReturn(null);
        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> {
                    endpointController.findEndpoint(NAAN, NAME, ENDPOINT_NAME, API_VERSION);
                });
        assertEquals(String.format("Cannot find endpoint with id %s/%s/%s/%s", NAAN, NAME, API_VERSION, ENDPOINT_NAME),
                activatorException.getMessage());
    }

    private EndpointResource createEndpointResource(Endpoint endpoint) {
        EndpointResource resource = new EndpointResource(endpoint, "kos");

        return resource;
    }
}

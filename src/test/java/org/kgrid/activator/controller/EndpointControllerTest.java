package org.kgrid.activator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kgrid.activator.ActivatorException;
import org.kgrid.activator.services.ActivationService;
import org.kgrid.activator.services.Endpoint;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.shelf.domain.KnowledgeObjectWrapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.kgrid.activator.utils.KoCreationTestHelper.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EndpointControllerTest {
    @Mock
    private ActivationService activationService;
    @Mock
    private Map<URI, Endpoint> endpoints;
    @InjectMocks
    EndpointController endpointController;
    private KnowledgeObjectWrapper kow;
    private Endpoint endpoint;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(endpointController, "shelfRoot", "kos");
        kow = new KnowledgeObjectWrapper(generateMetadata(NAAN, NAME, VERSION));
        kow.addService(generateServiceNode());
        kow.addDeployment(getEndpointDeploymentJsonForEngine(JS_ENGINE, ENDPOINT_NAME));
        endpoint = new Endpoint(kow, ENDPOINT_NAME);
        when(endpoints.get(endpoint.getId())).thenReturn(endpoint);
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
        EndpointResource endpointResource = endpointController.findEndpoint(NAAN, NAME, ENDPOINT_NAME, API_VERSION);
        assertEquals(createEndpointResource(endpoint), endpointResource);
    }

    @Test
    public void testFindEndpointReturnsEndpointResourceWithErrorStatusForBadEndpoint() {
        ObjectNode dumbNode = new ObjectMapper().createObjectNode();
        dumbNode.put("trash", "also trash");
        kow.addService(dumbNode);
        kow.addDeployment(dumbNode);
        endpoint = new Endpoint(kow, ENDPOINT_NAME);
        endpoints.replace(endpoint.getId(), endpoint);
        EndpointResource endpointResource = endpointController.findEndpoint(NAAN, NAME, ENDPOINT_NAME, API_VERSION);
        assertEquals(String.format("Could not create endpoint resource for malformed endpoint: %s/%s/%s",
                NAAN, NAME, VERSION), endpointResource.getStatus());
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

    @Test
    public void testExecuteEndpointOldVersionCallsExecuteOnActivationService() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        String inputs = "inputs";
        endpointController.executeEndpointOldVersion(NAAN, NAME, API_VERSION, ENDPOINT_NAME, inputs, headers);
        verify(activationService).execute(endpoint.getId(), inputs, headers.get("Content-Type"));
    }

    @Test
    public void testExecuteEndpointOldVersionThrowsIfActivationServiceThrows() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        String inputs = "inputs";
        String adapterExceptionMessage = "Blammo";
        when(activationService.execute(endpoint.getId(), inputs, headers.get("Content-Type")))
                .thenThrow(new AdapterException(adapterExceptionMessage));

        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> {
                    endpointController.executeEndpointOldVersion(NAAN, NAME, API_VERSION, ENDPOINT_NAME, inputs, headers);
                });
        assertEquals(String.format("Exception for endpoint %s/%s/%s/%s %s",
                NAAN, NAME, API_VERSION, ENDPOINT_NAME, adapterExceptionMessage), activatorException.getMessage());
    }

    @Test
    public void testExecuteEndpointCallsExecuteOnActivationService() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        String inputs = "inputs";
        endpointController.executeEndpoint(NAAN, NAME, API_VERSION, ENDPOINT_NAME, inputs, headers);
        verify(activationService).execute(endpoint.getId(), inputs, headers.get("Content-Type"));
    }

    @Test
    public void testExecuteEndpointThrowsIfActivationServiceThrows() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        String inputs = "inputs";
        String adapterExceptionMessage = "Blammo";
        when(activationService.execute(endpoint.getId(), inputs, headers.get("Content-Type")))
                .thenThrow(new AdapterException(adapterExceptionMessage));

        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> {
                    endpointController.executeEndpoint(NAAN, NAME, API_VERSION, ENDPOINT_NAME, inputs, headers);
                });
        assertEquals(String.format("Exception for endpoint %s/%s/%s/%s %s",
                NAAN, NAME, API_VERSION, ENDPOINT_NAME, adapterExceptionMessage), activatorException.getMessage());
    }

    private EndpointResource createEndpointResource(Endpoint endpoint) {
        EndpointResource resource = new EndpointResource(endpoint, "kos");

        return resource;
    }
}

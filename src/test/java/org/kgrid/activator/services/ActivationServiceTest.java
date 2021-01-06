package org.kgrid.activator.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kgrid.activator.exceptions.ActivatorException;
import org.kgrid.activator.EndPointResult;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.ShelfResourceNotFound;
import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.kgrid.activator.utils.KoCreationTestHelper.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ActivationServiceTest {

    public static final URI OBJECT_LOCATION = URI.create("ObjectLocation");
    private static final String CONTENT_TYPE = "application/json";
    @Mock
    private Endpoint mockEndpoint;
    private Map<URI, Endpoint> endpointMap = new HashMap<>();
    @Mock
    private AdapterResolver adapterResolver;
    @Mock
    private KnowledgeObjectRepository koRepo;
    @Mock
    private Adapter adapter;
    @Mock
    private Executor executor;
    private ActivationService activationService;
    private JsonNode deploymentJson;
    private JsonNode deploymentService;
    private JsonNode metadata;
    private String input = "input";


    @Before
    public void setup() {
        deploymentJson = getEndpointDeploymentJsonForEngine(JS_ENGINE, ENDPOINT_NAME);
        deploymentService = generateServiceNode();
        metadata = generateMetadata(NAAN, NAME, VERSION);
        final URI uri = URI.create(String.format("%s/%s/%s/%s", NAAN, NAME, VERSION, ENDPOINT_NAME));
        when(adapterResolver.getAdapter(JS_ENGINE)).thenReturn(adapter);
        when(adapter.activate(any(), any(), any())).thenReturn(executor);
        when(koRepo.getObjectLocation(ARK_ID)).thenReturn(OBJECT_LOCATION);
        when(mockEndpoint.getService()).thenReturn(deploymentService);
        when(mockEndpoint.getDeployment()).thenReturn(deploymentJson.get("/" + ENDPOINT_NAME).get(POST_HTTP_METHOD));
        when(mockEndpoint.getArkId()).thenReturn(ARK_ID);
        when(mockEndpoint.getId()).thenReturn(uri);
        when(mockEndpoint.getExecutor()).thenReturn(executor);
        when(mockEndpoint.getMetadata()).thenReturn(metadata);
        when(mockEndpoint.getStatus()).thenReturn("GOOD");
        when(mockEndpoint.isActive()).thenReturn(true);
        when(mockEndpoint.getEndpointName()).thenReturn("welcome");
        endpointMap.put(uri, mockEndpoint);
        activationService = new ActivationService(adapterResolver, endpointMap, koRepo);
    }

    @Test
    public void activateGetsDeploymentFromEndpoint() {
        activationService.activate(endpointMap);
        verify(mockEndpoint).getDeployment();
    }

    @Test
    public void activateGetsAdapterFromResolver() {
        activationService.activate(endpointMap);
        verify(adapterResolver).getAdapter(JS_ENGINE);
    }

    @Test
    public void activateGetsKoLocationFromRepo() {
        activationService.activate(endpointMap);
        verify(koRepo).getObjectLocation(ARK_ID);
    }

    @Test
    public void activateCallsActivateOnAdapter() {
        activationService.activate(endpointMap);
        verify(adapter).activate(OBJECT_LOCATION,
                URI.create(NAAN + "/" + NAME + "/" + VERSION + "/" + ENDPOINT_NAME), deploymentJson.get("/" + ENDPOINT_NAME).get(POST_HTTP_METHOD));
    }

    @Test
    public void activateSetsExecutorInEndpointMap() {
        activationService.activate(endpointMap);
        verify(mockEndpoint).setExecutor(executor);
    }

    @Test
    public void activateDoesNotSetExecutorIfActivatorExceptionIsThrownAnywhere() {
        when(mockEndpoint.getDeployment()).thenReturn(null);
        activationService.activate(endpointMap);
        verify(mockEndpoint).setExecutor(null);
    }

    @Test
    public void activateCatchesExceptionsFromShelf() {
        when(koRepo.getObjectLocation(any())).thenThrow(new ShelfResourceNotFound("ope"));
        activationService.activate(endpointMap);
        verify(mockEndpoint).setExecutor(null);
    }

    @Test
    public void activateCatchesExceptionsFromAdapter() {
        String exceptionMessage = "ope";
        when(adapter.activate(any(), any(), any())).thenThrow(new AdapterException(exceptionMessage));
        activationService.activate(endpointMap);
        verify(mockEndpoint).setStatus("Adapter could not create executor: " + exceptionMessage);
    }

    @Test
    public void executeGetsExecutorFromEndpoint() {
        activationService.execute(mockEndpoint.getId(), "input", HttpMethod.POST, CONTENT_TYPE);
        verify(mockEndpoint).getExecutor();
    }

    @Test
    public void executeExecutesExecutor() {
        activationService.execute(mockEndpoint.getId(), input, HttpMethod.POST, CONTENT_TYPE);
        verify(executor).execute(input, CONTENT_TYPE);
    }

    @Test
    public void executeSetsInputOnEndpointResult() {
        EndPointResult result;
        result = activationService.execute(mockEndpoint.getId(), input, HttpMethod.POST, CONTENT_TYPE);
        assertEquals(input, result.getInfo().get("inputs"));
    }

    @Test
    public void executeSetsMetadataOnEndpointResult() {
        EndPointResult result = activationService.execute(mockEndpoint.getId(), input, HttpMethod.POST, CONTENT_TYPE);
        assertEquals(metadata, result.getInfo().get("ko"));
    }

    @Test
    public void executeThrowsActivatorExceptionWhenEndpointIsNotInMap() {
        URI missingId = URI.create("not/in/map/garbage");

        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> {
                    activationService.execute(
                            missingId,
                            input, HttpMethod.POST, CONTENT_TYPE);
                });
        assertEquals("No active endpoint found for " + missingId, activatorException.getMessage());
    }

    @Test
    public void executeThrowsActivatorExceptionWhenEndpointIsNotActive() {
        when(mockEndpoint.isActive()).thenReturn(false);
        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> {
                    activationService.execute(
                            mockEndpoint.getId(),
                            input, HttpMethod.POST, CONTENT_TYPE);
                });
        assertEquals("No active endpoint found for " + mockEndpoint.getId(), activatorException.getMessage());
    }

    @Test
    public void activateSetsEndpointStatusToActivated() {
        activationService.activate(endpointMap);
        verify(mockEndpoint).setStatus("Activated");
    }

    @Test
    public void activateSetsEndpointStatusToCouldNotBeActivatedWithMessage() {
        when(mockEndpoint.getDeployment()).thenReturn(null);
        activationService.activate(endpointMap);
        verify(mockEndpoint).setStatus(String.format("Could not be activated: No deployment specification for %s/%s/%s/%s", NAAN, NAME, VERSION, ENDPOINT_NAME));
    }
}

package org.kgrid.activator.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kgrid.activator.ActivatorException;
import org.kgrid.activator.EndPointResult;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.ShelfResourceNotFound;
import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
    private JsonNode metadata;
    private String input = "input";


    @Before
    public void setup() {
        deploymentJson = getEndpointDeploymentJson();
        metadata = generateMetadata();
        final URI uri = URI.create(String.format("%s/%s/%s/%s", NAAN, NAME, VERSION, ENDPOINT_NAME));
        when(adapterResolver.getAdapter(ENGINE)).thenReturn(adapter);
        when(adapter.activate(any(), any(), any())).thenReturn(executor);
        when(koRepo.getObjectLocation(ARK_ID)).thenReturn(OBJECT_LOCATION);
        when(mockEndpoint.getDeployment()).thenReturn(deploymentJson.get(ENDPOINT_NAME).get(POST_HTTP_METHOD));
        when(mockEndpoint.getArkId()).thenReturn(ARK_ID);
        when(mockEndpoint.getId()).thenReturn(uri);
        when(mockEndpoint.getExecutor()).thenReturn(executor);
        when(mockEndpoint.getMetadata()).thenReturn(metadata);
        when(mockEndpoint.getStatus()).thenReturn("GOOD");
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
        verify(adapterResolver).getAdapter(ENGINE);
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
                URI.create(NAAN + "/" + NAME + "/" + VERSION + "/" + ENDPOINT_NAME), deploymentJson.get(ENDPOINT_NAME).get(POST_HTTP_METHOD));
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
        activationService.execute(mockEndpoint.getId(), "input");
        verify(mockEndpoint).getExecutor();
    }

    @Test
    public void executeExecutesExecutor() {
        activationService.execute(mockEndpoint.getId(), input);
        verify(executor).execute(input);
    }

    @Test
    public void executeSetsInputOnEndpointResult() {
        EndPointResult result;
        result = activationService.execute(mockEndpoint.getId(), input);
        assertEquals(input, result.getInfo().get("inputs"));
    }

    @Test
    public void executeSetsMetadataOnEndpointResult() {
        EndPointResult result = activationService.execute(mockEndpoint.getId(), input);
        assertEquals(metadata, result.getInfo().get("ko"));
    }

    @Test
    public void executeThrowsActivatorExceptionWhenEndpointIsNotInMap() {
        URI missingId = URI.create("not/in/map/garbage");

        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> {
                    activationService.execute(
                            missingId,
                            input);
                });
        assertEquals("No endpoint found for " + missingId, activatorException.getMessage());
    }

    @Test
    public void executeThrowsActivatorExceptionWhenExecutorNotFound() {
        when(mockEndpoint.getExecutor()).thenReturn(null);
        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> {
                    activationService.execute(
                            mockEndpoint.getId(),
                            input);
                });
        assertEquals("No executor found for " + mockEndpoint.getId(), activatorException.getMessage());
    }

}

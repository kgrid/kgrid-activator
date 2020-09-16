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
import org.kgrid.adapter.v8.V8Executor;
import org.kgrid.shelf.ShelfResourceNotFound;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
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
    private EndpointId endpointId1;
    private Endpoint endpoint1 = Mockito.mock(Endpoint.class);
    private Map<EndpointId, Endpoint> endpointMap = new HashMap<>();
    @Mock
    private AdapterResolver adapterResolver;
    @Mock
    private KnowledgeObjectRepository koRepo;
    @Mock
    private Adapter adapter;
    @Mock
    private V8Executor executor;
    private ActivationService activationService;
    private JsonNode deploymentJson;
    private JsonNode metadata;
    private String input = "input";


    @Before
    public void setup() throws IOException {
        endpointId1 = new EndpointId(ARK_ID, ENDPOINT_NAME);
        endpointMap.put(endpointId1, endpoint1);
        activationService = new ActivationService(adapterResolver, endpointMap, koRepo);
        deploymentJson = getEndpointDeploymentJson();
        metadata = generateMetadata();

        when(endpoint1.getDeployment()).thenReturn(deploymentJson);
        when(endpoint1.getArkId()).thenReturn(ARK_ID);
        when(endpoint1.getExecutor()).thenReturn(executor);
        when(adapterResolver.getAdapter(ADAPTER)).thenReturn(adapter);
        when(adapter.activate(any(), any(), any(), any(), any(), any())).thenReturn(executor);
        when(koRepo.getObjectLocation(ARK_ID)).thenReturn(OBJECT_LOCATION);
        when(endpoint1.getMetadata()).thenReturn(metadata);
        when(endpoint1.getStatus()).thenReturn("GOOD");
    }

    @Test
    public void activateGetsDeploymentFromEndpoint() {
        activationService.activate(endpointMap);
        verify(endpoint1).getDeployment();
    }

    @Test
    public void activateGetsAdapterFromResolver() {
        activationService.activate(endpointMap);
        verify(adapterResolver).getAdapter(ADAPTER);
    }

    @Test
    public void activateGetsKoLocationFromRepo() {
        activationService.activate(endpointMap);
        verify(koRepo).getObjectLocation(ARK_ID);
    }

    @Test
    public void activateCallsActivateOnAdapter() {
        activationService.activate(endpointMap);
        verify(adapter).activate(OBJECT_LOCATION, ARK_ID.getNaan(), ARK_ID.getName(), ARK_ID.getVersion(), ENDPOINT_NAME.substring(1), deploymentJson);
    }

    @Test
    public void activateSetsExecutorInEndpointMap() {
        activationService.activate(endpointMap);
        verify(endpoint1).setExecutor(executor);
    }

    @Test
    public void activateDoesNotSetExecutorIfActivatorExceptionIsThrownAnywhere() {
        when(endpoint1.getDeployment()).thenReturn(null);
        activationService.activate(endpointMap);
        verify(endpoint1).setExecutor(null);
    }

    @Test
    public void activateCatchesExceptionsFromShelf() {
        when(koRepo.getObjectLocation(any())).thenThrow(new ShelfResourceNotFound("ope"));
        activationService.activate(endpointMap);
        verify(endpoint1).setExecutor(null);
    }

    @Test
    public void activateCatchesExceptionsFromAdapter() {
        String exceptionMessage = "ope";
        when(adapter.activate(any(), any(), any(), any(), any(), any())).thenThrow(new AdapterException(exceptionMessage));
        activationService.activate(endpointMap);
        verify(endpoint1).setStatus("Adapter could not create executor: " + exceptionMessage);
    }

    @Test
    public void executeGetsExecutorFromEndpoint() {
        activationService.execute(endpointId1, VERSION, "input");
        verify(endpoint1).getExecutor();
    }

    @Test
    public void executeExecutesExecutor() {
        activationService.execute(endpointId1, VERSION, input);
        verify(executor).execute(input);
    }

    @Test
    public void executeSetsInputOnEndpointResult() {
        EndPointResult result;
        result = activationService.execute(endpointId1, VERSION, input);
        assertEquals(input, result.getInfo().get("inputs"));
    }

    @Test
    public void executeSetsMetadataOnEndpointResult() {
        EndPointResult result = activationService.execute(endpointId1, VERSION, input);
        assertEquals(metadata, result.getInfo().get("ko"));
    }

    @Test
    public void executeThrowsActivatorExceptionWhenEndpointIsNotInMap() {
        EndpointId missingId = new EndpointId(new ArkId("not", "in", "map"), "garbage");

        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> {
                    activationService.execute(
                            missingId,
                            VERSION, input);
                });
        assertEquals("No endpoint found for " + missingId, activatorException.getMessage());
    }

    @Test
    public void executeThrowsActivatorExceptionWhenExecutorNotFound() {
        when(endpoint1.getExecutor()).thenReturn(null);
        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> {
                    activationService.execute(
                            endpointId1,
                            VERSION, input);
                });
        assertEquals("No executor found for " + endpointId1, activatorException.getMessage());
    }

}
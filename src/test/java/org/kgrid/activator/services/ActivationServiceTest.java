package org.kgrid.activator.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kgrid.activator.EndPointResult;
import org.kgrid.activator.constants.EndpointStatus;
import org.kgrid.activator.domain.Endpoint;
import org.kgrid.activator.testUtilities.KoCreationTestHelper;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.ShelfResourceNotFound;
import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.kgrid.activator.testUtilities.KoCreationTestHelper.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ActivationServiceTest {

    public static final URI OBJECT_LOCATION = URI.create("ObjectLocation");
    private static final MediaType CONTENT_TYPE = MediaType.APPLICATION_JSON;
    private final Map<URI, Endpoint> endpointMap = new HashMap<>();
    @Mock
    private Endpoint mockEndpoint;
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
    private final String input = "input";

    @Before
    public void setup() {
        deploymentJson = getEndpointDeploymentJsonForEngine(JS_ENGINE, JS_ENDPOINT_NAME);
        metadata = generateMetadata(JS_NAAN, JS_NAME, JS_VERSION);
        EndPointResult endPointResult = new EndPointResult(null);
        endPointResult.getInfo().put("inputs", input);
        endPointResult.getInfo().put("ko", metadata);
        when(adapterResolver.getAdapter(JS_ENGINE)).thenReturn(adapter);
        when(adapter.activate(any(), any(), any())).thenReturn(executor);
        when(koRepo.getObjectLocation(JS_ARK_ID)).thenReturn(OBJECT_LOCATION);
        when(mockEndpoint.getDeployment()).thenReturn(deploymentJson.get("/" + JS_ENDPOINT_NAME).get(POST_HTTP_METHOD));
        when(mockEndpoint.getArkId()).thenReturn(JS_ARK_ID);
        when(mockEndpoint.getStatus()).thenReturn(EndpointStatus.LOADED.name());
        when(mockEndpoint.getEngine()).thenReturn(JS_ENGINE);
        endpointMap.put(JS_ENDPOINT_URI, mockEndpoint);
        activationService = new ActivationService(adapterResolver, endpointMap, koRepo);
    }

    @Test
    public void activateGetsDeploymentFromEndpoint() {
        activationService.activateEndpoints(endpointMap);
        verify(mockEndpoint).getDeployment();
    }

    @Test
    public void activateGetsAdapterFromResolver() {
        activationService.activateEndpoints(endpointMap);
        verify(adapterResolver).getAdapter(JS_ENGINE);
    }

    @Test
    public void activateGetsKoLocationFromRepo() {
        activationService.activateEndpoints(endpointMap);
        verify(koRepo).getObjectLocation(JS_ARK_ID);
    }

    @Test
    public void activateCallsActivateOnAdapter() {
        activationService.activateEndpoints(endpointMap);
        verify(adapter).activate(OBJECT_LOCATION,
                JS_ENDPOINT_URI,
                deploymentJson.get("/" + JS_ENDPOINT_NAME).get(POST_HTTP_METHOD));
    }

    @Test
    public void activateSetsExecutorInEndpointMap() {
        activationService.activateEndpoints(endpointMap);
        verify(mockEndpoint).setExecutor(executor);
    }

    @Test
    public void activateDoesNotSetExecutorIfActivatorExceptionIsThrownAnywhere() {
        when(mockEndpoint.getDeployment()).thenReturn(null);
        when(adapter.activate(any(), any(), any())).thenThrow(new AdapterException(""));
        activationService.activateEndpoints(endpointMap);
        verify(mockEndpoint).setExecutor(null);
    }

    @Test
    public void activateCatchesExceptionsFromShelf() {
        when(koRepo.getObjectLocation(any())).thenThrow(new ShelfResourceNotFound("ope"));
        activationService.activateEndpoints(endpointMap);
        verify(mockEndpoint).setExecutor(null);
    }

    @Test
    public void activateCatchesExceptionsFromAdapter() {
        String exceptionMessage = "ope";
        when(adapter.activate(any(), any(), any())).thenThrow(new AdapterException(exceptionMessage));
        activationService.activateEndpoints(endpointMap);
        verify(mockEndpoint).setStatus(String.format("Could not activate %s. Cause: %s",
                KoCreationTestHelper.JS_ENDPOINT_ID, exceptionMessage));
    }

    @Test
    public void activateSetsEndpointStatusToActivated() {
        activationService.activateEndpoints(endpointMap);
        verify(mockEndpoint).setStatus(EndpointStatus.ACTIVATED.name());
    }

    @Test
    public void activateSetsEndpointStatusToCouldNotBeActivatedWithMessage() {
        when(mockEndpoint.getDeployment()).thenReturn(null);
        String message = "bang";
        when(adapter.activate(any(), any(), any())).thenThrow(new AdapterException(message));
        activationService.activateEndpoints(endpointMap);
        verify(mockEndpoint).setStatus(String.format(
                "Could not activate %s. Cause: %s",
                KoCreationTestHelper.JS_ENDPOINT_ID, message));
    }
}

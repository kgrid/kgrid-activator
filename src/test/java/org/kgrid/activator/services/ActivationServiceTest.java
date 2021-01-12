package org.kgrid.activator.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kgrid.activator.EndPointResult;
import org.kgrid.activator.exceptions.ActivatorException;
import org.kgrid.activator.exceptions.ActivatorUnsupportedMediaTypeException;
import org.kgrid.activator.utils.KoCreationTestHelper;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.ShelfResourceNotFound;
import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.kgrid.activator.utils.KoCreationTestHelper.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ActivationServiceTest {

    public static final URI OBJECT_LOCATION = URI.create("ObjectLocation");
    private static final MediaType CONTENT_TYPE = MediaType.APPLICATION_JSON;
    @Mock
    private Endpoint mockEndpoint;
    private final Map<URI, Endpoint> endpointMap = new HashMap<>();
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
        deploymentJson = getEndpointDeploymentJsonForEngine(JS_ENGINE, ENDPOINT_NAME);
        metadata = generateMetadata(NAAN, NAME, VERSION);
        EndPointResult endPointResult = new EndPointResult(null);
        endPointResult.getInfo().put("inputs", input);
        endPointResult.getInfo().put("ko", metadata);
        when(adapterResolver.getAdapter(JS_ENGINE)).thenReturn(adapter);
        when(adapter.activate(any(), any(), any())).thenReturn(executor);
        when(koRepo.getObjectLocation(ARK_ID)).thenReturn(OBJECT_LOCATION);
        when(mockEndpoint.getDeployment()).thenReturn(deploymentJson.get("/" + ENDPOINT_NAME).get(POST_HTTP_METHOD));
        when(mockEndpoint.getArkId()).thenReturn(ARK_ID);
        when(mockEndpoint.getId()).thenReturn(ENDPOINT_URI);
        when(mockEndpoint.getStatus()).thenReturn("GOOD");
        when(mockEndpoint.isActive()).thenReturn(true);
        when(mockEndpoint.getEngine()).thenReturn(JS_ENGINE);
        when(mockEndpoint.isSupportedContentType(KoCreationTestHelper.CONTENT_TYPE)).thenReturn(true);
        when(mockEndpoint.execute(input, CONTENT_TYPE)).thenReturn(endPointResult);
        endpointMap.put(ENDPOINT_URI, mockEndpoint);
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
        verify(koRepo).getObjectLocation(ARK_ID);
    }

    @Test
    public void activateCallsActivateOnAdapter() {
        activationService.activateEndpoints(endpointMap);
        verify(adapter).activate(OBJECT_LOCATION,
                ENDPOINT_URI,
                deploymentJson.get("/" + ENDPOINT_NAME).get(POST_HTTP_METHOD));
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
                KoCreationTestHelper.ENDPOINT_ID, exceptionMessage));
    }


    @Test
    public void activateSetsEndpointStatusToActivated() {
        activationService.activateEndpoints(endpointMap);
        verify(mockEndpoint).setStatus("Activated");
    }

    @Test
    public void activateSetsEndpointStatusToCouldNotBeActivatedWithMessage() {
        when(mockEndpoint.getDeployment()).thenReturn(null);
        String message = "bang";
        when(adapter.activate(any(), any(), any())).thenThrow(new AdapterException(message));
        activationService.activateEndpoints(endpointMap);
        verify(mockEndpoint).setStatus(String.format(
                "Could not activate %s. Cause: %s",
                KoCreationTestHelper.ENDPOINT_ID, message));
    }

    @Test
    public void executeExecutesExecutor() {
        activationService.execute(mockEndpoint.getId(), input, HttpMethod.POST, CONTENT_TYPE);
        verify(mockEndpoint).execute(input, CONTENT_TYPE);
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
    public void execute_ValidatesContentHeader() {
        activationService.execute(mockEndpoint.getId(), input, HttpMethod.POST, CONTENT_TYPE);
        verify(mockEndpoint).isSupportedContentType(CONTENT_TYPE);
    }

    @Test
    public void execute_ThrowsIfInvalidContentHeader() {
        when(mockEndpoint.isSupportedContentType(CONTENT_TYPE)).thenReturn(false);
        ActivatorUnsupportedMediaTypeException exception =
                Assert.assertThrows(ActivatorUnsupportedMediaTypeException.class,
                () -> activationService.execute(mockEndpoint.getId(), input, HttpMethod.POST, CONTENT_TYPE));
        assertEquals(String.format("Endpoint %s does not support media type %s. Supported Content Types: %s",
                ENDPOINT_ID, CONTENT_TYPE, mockEndpoint.getSupportedContentTypes()), exception.getMessage());
    }
}

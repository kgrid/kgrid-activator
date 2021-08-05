package org.kgrid.activator.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kgrid.activator.domain.EndPointResult;
import org.kgrid.activator.domain.Endpoint;
import org.kgrid.activator.exceptions.ActivatorEndpointNotFoundException;
import org.kgrid.activator.services.ActivationService;
import org.kgrid.adapter.api.ClientRequest;
import org.kgrid.adapter.api.Executor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;

import javax.activation.MimetypesFileTypeMap;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.kgrid.activator.testUtilities.KoCreationTestHelper.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Request Controller Tests")
public class RequestControllerTest {

    @Mock
    private ActivationService activationService;
    @Mock
    private MimetypesFileTypeMap fileTypeMap;
    @InjectMocks
    private RequestController requestController;
    private final String INPUT = "input";
    private final String OUTPUT = "output";
    private final String RESOURCE_NAME = "file.csv";
    private final String RESOURCE_SLUG = "/source/" + RESOURCE_NAME;
    private final URI FULL_RESOURCE_URI = URI.create(JS_ENDPOINT_URI + RESOURCE_SLUG);
    private final InputStream resourceInputStream = mock(InputStream.class);
    private final HttpHeaders headers = new HttpHeaders();
    private final Endpoint endpoint = mock(Endpoint.class);
    private final RequestEntity requestEntity = mock(RequestEntity.class);

    @BeforeEach
    public void setup() throws IOException {
        headers.setContentType(CONTENT_TYPE);
        ArrayList<String> contentTypes = new ArrayList<>();
        contentTypes.add(CONTENT_TYPE.toString());
        contentTypes.add(MediaType.IMAGE_GIF.toString());
        lenient().when(activationService.getDefaultEndpoint(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME))
                .thenReturn(endpoint);
        lenient().when(activationService.getEndpoint(JS_ENDPOINT_URI)).thenReturn(endpoint);
        when(endpoint.isActive()).thenReturn(true);
        when(endpoint.getApiVersion()).thenReturn(JS_API_VERSION);
        when(endpoint.getExecutor()).thenReturn(new Executor() {
            @Override
            public Object execute(ClientRequest request) {
                return OUTPUT;
            }
        });
        when(endpoint.getSupportedContentTypes()).thenReturn(contentTypes);
        when(requestEntity.getUrl()).thenReturn(FULL_RESOURCE_URI);
        when(requestEntity.getHeaders()).thenReturn(headers);
        when(requestEntity.getMethod()).thenReturn(HttpMethod.POST);
        when(resourceInputStream.readAllBytes()).thenReturn(OUTPUT.getBytes());
    }

    @Test
    @DisplayName("ExecuteEndpointPathVersion gets default endpoint")
    public void testExecuteEndpointPathVersion_GetsDefaultEndpoint() {
        requestController.executeEndpointPathVersion(
                JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME, requestEntity);
        verify(activationService).getDefaultEndpoint(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME);
    }

    @Test
    @DisplayName("ExecuteEndpointPathVersion returns result")
    public void testExecuteEndpointPathVersion_ReturnsResult_MINIMAL() {
        headers.setAccept(List.of(MediaType.valueOf("application/json;profile=\"minimal\"")));
        ResponseEntity result = requestController.executeEndpointPathVersion(
                JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME, requestEntity);
        assertEquals(OUTPUT, result.getBody());
    }

    @Test
    @DisplayName("ExecuteEndpointPathVersion returns result")
    public void testExecuteEndpointPathVersion_ReturnsResult_MAXIMAL() {
        EndPointResult endPointResult = (EndPointResult) requestController.executeEndpointPathVersion(
                JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME, requestEntity).getBody();
        assertEquals(OUTPUT, endPointResult.getResult());
    }

    @Test
    @DisplayName("ExecuteEndpointPathVersion throws if a null executor is returned from the endpoint")
    public void testExecuteEndpointPathVersion_ThrowsIfNullExecutorIsReturned() {
        when(endpoint.getExecutor()).thenReturn(null);
        Exception ex = assertThrows(ActivatorEndpointNotFoundException.class,
                () -> requestController.executeEndpointPathVersion(
                        JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME, requestEntity));
        assertAll(
                () -> assertEquals("No executor found for " + endpoint.getId(), ex.getMessage())
        );
    }

    @Test
    @DisplayName("Execute endpoint inactive throws")
    public void testExecuteEndpoint_ThrowsIfEndpointIsNotActive() {
        ArrayList<Endpoint> versions = new ArrayList<>();
        versions.add(endpoint);
        when(endpoint.isActive()).thenReturn(false);
        when(endpoint.getId()).thenReturn(JS_ENDPOINT_URI);

        ActivatorEndpointNotFoundException activatorException =
                assertThrows(ActivatorEndpointNotFoundException.class, () -> {
            requestController.executeEndpointQueryVersion(
                    JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME, requestEntity);
        });
        assertEquals(String.format("No executor found for naan/name/jsApiVersion/endpoint",
                JS_ENDPOINT_ID, JS_API_VERSION), activatorException.getMessage());
    }

    @Test
    @DisplayName("GetResourceEndpoint gets default endpoint")
    public void testGetResourceEndpoint_GetsDefaultEndpoint() {
        requestController.getResourceEndpoint(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME, requestEntity);
        verify(activationService).getDefaultEndpoint(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME);
    }

    @Test
    @DisplayName("ExecuteResourceEndpoint gets default endpoint")
    public void testExecuteResourceEndpoint_GetsDefaultEndpoint() {
        when(endpoint.getExecutor()).thenReturn(new Executor() {
            @Override
            public Object execute(ClientRequest request) {
                return resourceInputStream;
            }
        });
        requestController.executeResourceEndpoint(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME, requestEntity);
        verify(activationService).getDefaultEndpoint(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME);
    }

    @Test
    @DisplayName("ExecuteResourceEndpoint returns correct result")
    public void testExecuteResourceEndpoint_ReturnsCorrectResult() throws IOException {
        headers.setAccept(List.of(MediaType.valueOf("text/csv")));
        when(fileTypeMap.getContentType(RESOURCE_SLUG.substring(1))).thenReturn("text/csv");
        when(endpoint.getExecutor()).thenReturn(new Executor() {
            @Override
            public Object execute(ClientRequest request) {
                return resourceInputStream;
            }
        });
        InputStreamResource result = (InputStreamResource) requestController.executeResourceEndpoint(
                JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME, requestEntity).getBody();
        assertEquals(OUTPUT, new String(result.getInputStream().readAllBytes()));
    }
}

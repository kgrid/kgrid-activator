package org.kgrid.activator.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kgrid.activator.domain.EndPointResult;
import org.kgrid.activator.domain.Endpoint;
import org.kgrid.activator.exceptions.ActivatorEndpointNotFoundException;
import org.kgrid.activator.exceptions.ActivatorUnsupportedMediaTypeException;
import org.kgrid.activator.services.ActivationService;
import org.kgrid.adapter.api.ClientRequest;
import org.kgrid.adapter.api.Executor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.kgrid.activator.constants.CustomHeaders.ACCEPT_JSON_MINIMAL;
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
    private final InputStream resourceInputStream = Mockito.mock(InputStream.class);
    private final HttpHeaders headers = new HttpHeaders();
    private final Endpoint endpoint = Mockito.mock(Endpoint.class);
    private final HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);

    @BeforeEach
    public void setup() throws IOException {
        headers.setContentType(CONTENT_TYPE);
        ArrayList<String> contentTypes = new ArrayList<>();
        contentTypes.add(CONTENT_TYPE.toString());
        contentTypes.add(MediaType.IMAGE_GIF.toString());
        lenient().when(activationService.getDefaultEndpoint(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME)).thenReturn(endpoint);
        lenient().when(activationService.getEndpoint(JS_ENDPOINT_URI)).thenReturn(endpoint);
        when(endpoint.isActive()).thenReturn(true);
        when(endpoint.getApiVersion()).thenReturn(JS_API_VERSION);
        when(endpoint.isSupportedContentType(CONTENT_TYPE)).thenReturn(true);
        when(endpoint.getExecutor()).thenReturn(new Executor() {
            @Override
            public Object execute(ClientRequest request) {
                return OUTPUT;
            }
        });
        when(endpoint.getSupportedContentTypes()).thenReturn(contentTypes);
        when(servletRequest.getRequestURI()).thenReturn(FULL_RESOURCE_URI.toString());
        when(resourceInputStream.readAllBytes()).thenReturn(OUTPUT.getBytes());
    }

    @Test
    @DisplayName("ExecuteEndpointPathVersion gets default endpoint")
    public void testExecuteEndpointPathVersion_GetsDefaultEndpoint() {
        requestController.executeEndpointPathVersion(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME, INPUT, headers);
        verify(activationService).getDefaultEndpoint(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME);
    }

    @Test
    @DisplayName("ExecuteEndpointPathVersion returns result")
    public void testExecuteEndpointPathVersion_ReturnsResult_MINIMAL() {
        headers.setAccept(List.of(ACCEPT_JSON_MINIMAL.getValue()));
        Object result = requestController.executeEndpointPathVersion(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME, INPUT, headers);
        assertEquals(OUTPUT, result);
    }

    @Test
    @DisplayName("ExecuteEndpointPathVersion returns result")
    public void testExecuteEndpointPathVersion_ReturnsResult_MAXIMAL() {
        EndPointResult endPointResult = (EndPointResult) requestController.executeEndpointPathVersion(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME, INPUT, headers);
        assertEquals(OUTPUT, endPointResult.getResult());
    }

    @Test
    @DisplayName("ExecuteEndpointPathVersion throws if a null executor is returned from the endpoint")
    public void testExecuteEndpointPathVersion_ThrowsIfNullExecutorIsReturned() {
        when(endpoint.getExecutor()).thenReturn(null);
        Exception ex = assertThrows(ActivatorEndpointNotFoundException.class,
                () -> requestController.executeEndpointPathVersion(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME, INPUT, headers));
        assertAll(
                () -> assertEquals("No executor found for " + endpoint.getId(), ex.getMessage())
        );
    }

    @Test
    @DisplayName("Execute endpoint unsupported content type throws")
    public void testExecuteEndpoint_ThrowsIfUnsupportedContentType() {
        when(endpoint.isSupportedContentType(CONTENT_TYPE)).thenReturn(false);
        ActivatorUnsupportedMediaTypeException activatorException = assertThrows(ActivatorUnsupportedMediaTypeException.class, () -> {
            requestController.executeEndpointQueryVersion(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME, INPUT, headers);
        });
        assertEquals(String.format("Endpoint %s does not support media type %s. Supported Content Types: %s",
                endpoint.getId(), CONTENT_TYPE, endpoint.getSupportedContentTypes()), activatorException.getMessage());
    }

    @Test
    @DisplayName("Execute endpoint inactive throws")
    public void testExecuteEndpoint_ThrowsIfEndpointIsNotActive() {
        ArrayList<Endpoint> versions = new ArrayList<>();
        versions.add(endpoint);
        when(activationService.getAllVersions(JS_NAAN, JS_NAME, JS_ENDPOINT_NAME)).thenReturn(versions);
        when(endpoint.isActive()).thenReturn(false);
        when(endpoint.getId()).thenReturn(JS_ENDPOINT_URI);

        ActivatorEndpointNotFoundException activatorException = assertThrows(ActivatorEndpointNotFoundException.class, () -> {
            requestController.executeEndpointQueryVersion(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME, INPUT, headers);
        });
        assertEquals(String.format("No active endpoint found for %s Try one of these available versions: %s",
                JS_ENDPOINT_ID, JS_API_VERSION), activatorException.getMessage());
    }

    @Test
    @DisplayName("GetResourceEndpoint gets default endpoint")
    public void testGetResourceEndpoint_GetsDefaultEndpoint() {
        requestController.getResourceEndpoint(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME, headers);
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
        requestController.executeResourceEndpoint(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME, headers, servletRequest);
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
        InputStreamResource result = (InputStreamResource) requestController.executeResourceEndpoint(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME, headers, servletRequest).getBody();
        assertEquals(OUTPUT, new String(result.getInputStream().readAllBytes()));
    }

    @Test
    @DisplayName("Resource endpoint invalid accept type")
    public void testExecuteResourceEndpoint_InvalidAcceptType() {
        when(fileTypeMap.getContentType(RESOURCE_SLUG.substring(1))).thenReturn("text/csv");
        headers.remove("Content-Type");
        headers.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
        ResponseEntity<Object> response = requestController.executeResourceEndpoint(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME, headers, servletRequest);
        assertAll(
                () -> assertEquals(new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE), response)
        );
    }
}

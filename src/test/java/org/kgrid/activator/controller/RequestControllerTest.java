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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.kgrid.activator.testUtilities.KoCreationTestHelper.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private final URI FULL_RESOURCE_URI = URI.create(JS_ENDPOINT_URI.toString() + RESOURCE_SLUG);
    private final InputStream resourceInputStream = Mockito.mock(InputStream.class);
    private final EndPointResult resourceEndpointResult = new EndPointResult(resourceInputStream);
    private EndPointResult endpointResult = new EndPointResult(OUTPUT);
    private HttpHeaders headers = new HttpHeaders();
    private Endpoint endpoint = Mockito.mock(Endpoint.class);
    private HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);

    @BeforeEach
    public void setup() {
        Map<URI, Endpoint> endpointMap = new TreeMap<>();
        endpointMap.put(JS_ENDPOINT_URI, endpoint);
        headers.setContentType(CONTENT_TYPE);
        ArrayList<String> contentTypes = new ArrayList<>();
        contentTypes.add(CONTENT_TYPE.toString());
        contentTypes.add(MediaType.IMAGE_GIF.toString());
        when(activationService.createEndpointId(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME)).thenReturn(JS_ENDPOINT_URI);
        Mockito.lenient().when(activationService.getEndpoint(JS_ENDPOINT_URI)).thenReturn(endpoint);
        when(endpoint.isActive()).thenReturn(true);
        when(endpoint.getApiVersion()).thenReturn(JS_API_VERSION);
        when(endpoint.isSupportedContentType(CONTENT_TYPE)).thenReturn(true);
        when(endpoint.execute(INPUT, CONTENT_TYPE)).thenReturn(endpointResult);
        when(endpoint.execute(RESOURCE_SLUG.substring(1), null)).thenReturn(resourceEndpointResult);
        when(endpoint.getSupportedContentTypes()).thenReturn(contentTypes);
        when(servletRequest.getRequestURI()).thenReturn(FULL_RESOURCE_URI.toString());
    }

    @Test
    @DisplayName("Execute endpoint interactions")
    public void testExecuteEndpointInteractionsAndResult() {
        EndPointResult actualResult = requestController.executeEndpointQueryVersion(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME, INPUT, headers);
        assertAll(
                () -> verify(activationService).getEndpoint(JS_ENDPOINT_URI),
                () -> verify(endpoint).isActive(),
                () -> verify(endpoint).isSupportedContentType(CONTENT_TYPE),
                () -> verify(endpoint).execute(INPUT, CONTENT_TYPE),
                () -> assertSame(endpointResult.getResult(), actualResult.getResult())
        );
    }

    @Test
    @DisplayName("Resource endpoint interactions")
    public void testExecuteResourceEndpointInteractionsAndResult() {
        headers.remove("Content-Type");
        requestController.executeResourceEndpoint(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME, headers, servletRequest);
        assertAll(
                () -> verify(activationService).createEndpointId(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME),
                () -> verify(endpoint).execute(RESOURCE_SLUG.substring(1), null)
        );
    }

    @Test
    @DisplayName("Resource endpoint invalid accept type")
    public void testExecuteResourceEndpoint_InvalidAcceptType() {
        when(fileTypeMap.getContentType(RESOURCE_SLUG.substring(1))).thenReturn("text/csv");
        headers.remove("Content-Type");
        headers.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
        ResponseEntity<Object> response = requestController.executeResourceEndpoint(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME, headers, servletRequest);
        assertAll(
                () -> verify(activationService).createEndpointId(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME),
                () -> assertEquals(new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE), response)
        );
    }

    @Test
    @DisplayName("Execute endpoint unsupposed content type throws")
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

        ActivatorEndpointNotFoundException activatorException = assertThrows(ActivatorEndpointNotFoundException.class, () -> {
            requestController.executeEndpointQueryVersion(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME, INPUT, headers);
        });
        assertEquals(String.format("No active endpoint found for %s Try one of these available versions: %s",
                JS_ENDPOINT_ID, JS_API_VERSION), activatorException.getMessage());
    }

    @Test
    @DisplayName("Execute endpoint old version")
    public void testExecuteEndpointOldVersion_callsExecuteOnEndpoint() {
        requestController.executeEndpointPathVersion(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME, INPUT, headers);
        verify(endpoint).execute(INPUT, CONTENT_TYPE);
    }

    @Test
    @DisplayName("Get available resources")
    public void testGetAvailableResourceEndpoints_callsExecuteOnEndpoint() {
        requestController.getResourceEndpoints(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME, headers);
        verify(endpoint).execute(null, CONTENT_TYPE);
    }
}

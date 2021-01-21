package org.kgrid.activator.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kgrid.activator.EndPointResult;
import org.kgrid.activator.utilities.EndpointHelper;
import org.kgrid.activator.exceptions.ActivatorEndpointNotFoundException;
import org.kgrid.activator.exceptions.ActivatorUnsupportedMediaTypeException;
import org.kgrid.activator.services.Endpoint;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.kgrid.activator.utils.KoCreationTestHelper.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RequestControllerTest {

    @Mock
    private EndpointHelper endpointHelper;
    @InjectMocks
    private RequestController requestController;
    private final String INPUT = "input";
    private final String OUTPUT = "output";
    private final String RESOURCE_NAME = "file.csv";
    private final String RESOURCE_SLUG = "/source/" + RESOURCE_NAME;
    private final URI FULL_RESOURCE_URI = URI.create(ENDPOINT_URI.toString() + RESOURCE_SLUG);
    private final InputStream resourceInputStream = Mockito.mock(InputStream.class);
    private final EndPointResult resourceEndpointResult = new EndPointResult(resourceInputStream);
    private EndPointResult endpointResult = new EndPointResult(OUTPUT);
    private HttpHeaders headers = new HttpHeaders();
    private Endpoint endpoint = Mockito.mock(Endpoint.class);
    private HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);

    @Before
    public void setup() {
        headers.setContentType(CONTENT_TYPE);
        ArrayList<String> contentTypes = new ArrayList<>();
        contentTypes.add(CONTENT_TYPE.toString());
        contentTypes.add(MediaType.IMAGE_GIF.toString());
        ArrayList<Endpoint> versions = new ArrayList<>();
        versions.add(endpoint);
        when(endpointHelper.getAllVersions(NAAN, NAME, ENDPOINT_NAME)).thenReturn(versions);
        when(endpointHelper.createEndpointId(NAAN, NAME, API_VERSION, ENDPOINT_NAME)).thenReturn(ENDPOINT_URI);
        when(endpointHelper.getEndpoint(ENDPOINT_URI)).thenReturn(endpoint);
        when(endpoint.isActive()).thenReturn(true);
        when(endpoint.getApiVersion()).thenReturn(API_VERSION);
        when(endpoint.isSupportedContentType(CONTENT_TYPE)).thenReturn(true);
        when(endpoint.execute(INPUT, CONTENT_TYPE)).thenReturn(endpointResult);
        when(endpoint.execute(RESOURCE_SLUG.substring(1), null)).thenReturn(resourceEndpointResult);
        when(endpoint.getSupportedContentTypes()).thenReturn(contentTypes);
        when(servletRequest.getRequestURI()).thenReturn(FULL_RESOURCE_URI.toString());
    }

    @Test
    public void testExecuteEndpoint_GetsEndpointFromEndpointHelper() {
        requestController.executeEndpointQueryVersion(NAAN, NAME, API_VERSION, ENDPOINT_NAME, INPUT, headers);
        verify(endpointHelper).getEndpoint(ENDPOINT_URI);
    }

    @Test
    public void testExecuteEndpoint_ChecksEndpointIsActive() {
        requestController.executeEndpointQueryVersion(NAAN, NAME, API_VERSION, ENDPOINT_NAME, INPUT, headers);
        verify(endpoint).isActive();
    }

    @Test
    public void testExecuteEndpoint_ValidatesContentType_WhenHttpMethodIsPost() {
        requestController.executeEndpointQueryVersion(NAAN, NAME, API_VERSION, ENDPOINT_NAME, INPUT, headers);
        verify(endpoint).isSupportedContentType(CONTENT_TYPE);
    }

    @Test
    public void testExecuteEndpoint_ThrowsIfUnsupportedContentType() {
        when(endpoint.isSupportedContentType(CONTENT_TYPE)).thenReturn(false);
        ActivatorUnsupportedMediaTypeException activatorException = assertThrows(ActivatorUnsupportedMediaTypeException.class, () -> {
            requestController.executeEndpointQueryVersion(NAAN, NAME, API_VERSION, ENDPOINT_NAME, INPUT, headers);
        });
        assertEquals(String.format("Endpoint %s does not support media type %s. Supported Content Types: %s",
                endpoint.getId(), CONTENT_TYPE, endpoint.getSupportedContentTypes()), activatorException.getMessage());
    }

    @Test
    public void testExecuteEndpoint_ThrowsIfEndpointIsNotActive() {
        when(endpoint.isActive()).thenReturn(false);
        ActivatorEndpointNotFoundException activatorException = assertThrows(ActivatorEndpointNotFoundException.class, () -> {
            requestController.executeEndpointQueryVersion(NAAN, NAME, API_VERSION, ENDPOINT_NAME, INPUT, headers);
        });
        assertEquals(String.format("No active endpoint found for %s Try one of these available versions: %s",
                ENDPOINT_ID, API_VERSION), activatorException.getMessage());
    }

    @Test
    public void testExecuteEndpoint_ReturnsEndpointResultFromEndpoint() {
        EndPointResult actualResult = requestController.executeEndpointQueryVersion(NAAN, NAME, API_VERSION, ENDPOINT_NAME, INPUT, headers);
        assertSame(endpointResult.getResult(), actualResult.getResult());
    }

    @Test
    public void testExecuteEndpoint_callsExecuteOnEndpoint() {
        requestController.executeEndpointQueryVersion(NAAN, NAME, API_VERSION, ENDPOINT_NAME, INPUT, headers);
        verify(endpoint).execute(INPUT, CONTENT_TYPE);
    }

    @Test
    public void testExecuteEndpointOldVersion_callsExecuteOnEndpoint() {
        requestController.executeEndpointPathVersion(NAAN, NAME, API_VERSION, ENDPOINT_NAME, INPUT, headers);
        verify(endpoint).execute(INPUT, CONTENT_TYPE);
    }

    @Test
    public void testGetAvailableResourceEndpoints_callsExecuteOnEndpoint() {
        requestController.getResourceEndpoints(NAAN, NAME, API_VERSION, ENDPOINT_NAME, headers);
        verify(endpoint).execute(null, CONTENT_TYPE);
    }

    @Test
    public void testExecuteResourceEndpoint_createsNewEndpointIdForResource() {
        headers.remove("Content-Type");
        requestController.executeResourceEndpoint(NAAN, NAME, API_VERSION, ENDPOINT_NAME, headers, servletRequest);
        verify(endpointHelper).createEndpointId(NAAN, NAME, API_VERSION, ENDPOINT_NAME);
    }

    @Test
    public void testExecuteResourceEndpoint_ExecutesEndpointWithArtifactNameForInput() {
        headers.remove("Content-Type");
        requestController.executeResourceEndpoint(NAAN, NAME, API_VERSION, ENDPOINT_NAME, headers, servletRequest);
        verify(endpoint).execute(RESOURCE_SLUG.substring(1), null);
    }

}
package org.kgrid.activator.utilities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kgrid.activator.domain.Endpoint;
import org.kgrid.activator.exceptions.ActivatorEndpointNotFoundException;
import org.kgrid.activator.services.ActivationService;
import org.kgrid.shelf.domain.KnowledgeObjectWrapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.activation.MimetypesFileTypeMap;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.kgrid.activator.testUtilities.KoCreationTestHelper.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Endpoint Helper Tests")
public class EndpointHelperTest {

    public static final String ARTIFACT_ZIP = "artifact.zip";
    public static final String APPLICATION_ZIP = "application/zip";
    @Mock
    private ActivationService activationService;
    @Mock
    private MimetypesFileTypeMap fileTypeMap;
    @InjectMocks
    EndpointHelper endpointHelper;
    Endpoint endpoint2;

    private final Endpoint endpoint = getEndpointForEngine(JS_ENGINE);
    private final Map<URI, Endpoint> endpointMap = new TreeMap<>();


    @BeforeEach
    public void setup() {
        URI endpoint2Uri = URI.create(String.format("%s/%s/%s/%s", JS_NAAN, JS_NAME, "2.0", JS_ENDPOINT_NAME));
        endpoint2 = new Endpoint(new KnowledgeObjectWrapper(generateMetadata(JS_NAAN, JS_NAME, "2.0")), JS_ENDPOINT_NAME);
        endpointMap.put(JS_ENDPOINT_URI, endpoint);
        endpointMap.put(endpoint2Uri, endpoint2);
    }

    @Test
    @DisplayName("Get default version gets default version from endpoint map")
    public void testGetDefaultVersion() {
        when(activationService.getEndpointMap()).thenReturn(endpointMap);
        String defaultVersion = endpointHelper.getDefaultVersion(JS_NAAN, JS_NAME, JS_ENDPOINT_NAME);

        assertAll(
                () -> verify(activationService).getEndpointMap(),
                () -> assertEquals(endpoint.getApiVersion(), defaultVersion)
        );
    }

    @Test
    @DisplayName("Get all versions gets all version for an endpoint from the endpoint map")
    public void testGetAllVersions() {
        when(activationService.getEndpointMap()).thenReturn(endpointMap);
        List<Endpoint> endpointVersions = endpointHelper.getAllVersions(JS_NAAN, JS_NAME, JS_ENDPOINT_NAME);

        assertAll(
                () -> verify(activationService).getEndpointMap(),
                () -> assertTrue(endpointVersions.contains(endpoint)),
                () -> assertTrue(endpointVersions.contains(endpoint2))
        );
    }

    @Test
    @DisplayName("Get all versions throws if no versions are found")
    public void testGetAllVersions_throwsIfNoVersionsFound() {
        ActivatorEndpointNotFoundException activatorException =
                assertThrows(ActivatorEndpointNotFoundException.class, () ->
                        endpointHelper.getAllVersions(JS_NAAN, JS_NAME, JS_ENDPOINT_NAME));
        assertEquals(String.format("No active endpoints found for %s/%s/%s",
                JS_NAAN, JS_NAME, JS_ENDPOINT_NAME), activatorException.getMessage());
    }

    @Test
    @DisplayName("Get content type gets content type from file type map")
    public void testGetContentType() {
        when(fileTypeMap.getContentType(ARTIFACT_ZIP)).thenReturn(APPLICATION_ZIP);
        String contentType = endpointHelper.getContentType(ARTIFACT_ZIP);

        assertAll(
                () -> verify(fileTypeMap).getContentType(ARTIFACT_ZIP),
                () -> assertEquals(APPLICATION_ZIP, contentType)
        );
    }

    @Test
    @DisplayName("Get content disposition gets dispo with no slash in the filename")
    public void testGetContentDisposition_NoSlashInFilename() {
        String contentDisposition = endpointHelper.getContentDisposition(ARTIFACT_ZIP);

        assertEquals(String.format("inline; filename=\"%s\"", ARTIFACT_ZIP), contentDisposition);
    }

    @Test
    @DisplayName("Get content disposition gets dispo with a package and slash in the filename")
    public void testGetContentDisposition_SlashInFilename() {
        String contentDisposition = endpointHelper.getContentDisposition("src/" + ARTIFACT_ZIP);

        assertEquals(String.format("inline; filename=\"%s\"", ARTIFACT_ZIP), contentDisposition);
    }

    @Test
    @DisplayName("Create endpoint Id returns a proper URI")
    public void testCreateEndpointId() {
        URI endpointId = endpointHelper.createEndpointId(JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME);

        assertEquals(JS_ENDPOINT_URI, endpointId);
    }

    @Test
    @DisplayName("Create endpoint Id returns a proper URI with default version if none supplied")
    public void testCreateEndpointId_getsDefaultVersion() {
        when(activationService.getEndpointMap()).thenReturn(endpointMap);
        URI endpointId = endpointHelper.createEndpointId(JS_NAAN, JS_NAME, null, JS_ENDPOINT_NAME);

        assertEquals(JS_ENDPOINT_URI, endpointId);
    }

    @Test
    @DisplayName("Get endpoint gets endpoint from endpoint map using the URI supplied to it")
    public void testGetEndpointCallsEndpointMap() {
        when(activationService.getEndpointMap()).thenReturn(endpointMap);
        Endpoint endpoint = endpointHelper.getEndpoint(JS_ENDPOINT_URI);

        assertEquals(this.endpoint, endpoint);

    }
}
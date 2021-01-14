package org.kgrid.activator.Utilities;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kgrid.activator.services.Endpoint;
import org.kgrid.shelf.domain.KnowledgeObjectWrapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.activation.MimetypesFileTypeMap;
import java.net.URI;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.kgrid.activator.utils.KoCreationTestHelper.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EndpointHelperTest {

    public static final String ARTIFACT_ZIP = "artifact.zip";
    public static final String APPLICATION_ZIP = "application/zip";
    @Mock
    private Map<URI, Endpoint> endpoints;
    @Mock
    private MimetypesFileTypeMap fileTypeMap;
    @InjectMocks
    EndpointHelper endpointHelper;

    private KnowledgeObjectWrapper kow =
            new KnowledgeObjectWrapper(generateMetadata(NAAN, NAME, VERSION));
    private Endpoint endpoint = new Endpoint(kow, ENDPOINT_NAME);

    @Before
    public void setup() {
        kow.addService(generateServiceNode());
        kow.addDeployment(getEndpointDeploymentJsonForEngine(JS_ENGINE, ENDPOINT_NAME));
        HashSet<Map.Entry<URI, Endpoint>> entrySet = new HashSet<>();
        entrySet.add(new AbstractMap.SimpleEntry<>(ENDPOINT_URI, endpoint));
        when(endpoints.entrySet()).thenReturn(entrySet);
        when(fileTypeMap.getContentType(ARTIFACT_ZIP)).thenReturn(APPLICATION_ZIP);
    }

    @Test
    public void testGetDefaultVersion() {
        String defaultVersion = endpointHelper.getDefaultVersion(NAAN, NAME, ENDPOINT_NAME);
        verify(endpoints).entrySet();
        assertEquals(endpoint.getApiVersion(), defaultVersion);
    }

    @Test
    public void testGetContentType() {
        String contentType = endpointHelper.getContentType(ARTIFACT_ZIP);
        verify(fileTypeMap).getContentType(ARTIFACT_ZIP);
        assertEquals(APPLICATION_ZIP, contentType);
    }

    @Test
    public void testGetContentDisposition_NoSlashInFilename() {
        String contentDisposition = endpointHelper.getContentDisposition(ARTIFACT_ZIP);
        assertEquals(String.format("inline; filename=\"%s\"", ARTIFACT_ZIP), contentDisposition);
    }

    @Test
    public void testGetContentDisposition_SlashInFilename() {
        String contentDisposition = endpointHelper.getContentDisposition("src/" + ARTIFACT_ZIP);
        assertEquals(String.format("inline; filename=\"%s\"", ARTIFACT_ZIP), contentDisposition);
    }

    @Test
    public void testCreateEndpointId(){
        URI endpointId = endpointHelper.createEndpointId(NAAN, NAME, API_VERSION, ENDPOINT_NAME);
        assertEquals(ENDPOINT_URI, endpointId);
    }
}
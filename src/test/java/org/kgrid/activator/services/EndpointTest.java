package org.kgrid.activator.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kgrid.activator.EndPointResult;
import org.kgrid.activator.exceptions.ActivatorEndpointNotFoundException;
import org.kgrid.activator.utils.KoCreationTestHelper;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.domain.KnowledgeObjectWrapper;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.kgrid.activator.utils.KoCreationTestHelper.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EndpointTest {

    @Mock
    Executor executor;

    Endpoint endpoint;
    KnowledgeObjectWrapper wrapper;

    private String input = "input";
    private String result = "result";
    private final JsonNode metadata = generateMetadata(JS_NAAN, JS_NAME, JS_VERSION);

    @Before
    public void setUp() {

        wrapper = new KnowledgeObjectWrapper(metadata);
        wrapper.addService(KoCreationTestHelper.generateServiceNode(JS_ENGINE));
        wrapper.addDeployment(KoCreationTestHelper.getEndpointDeploymentJsonForEngine(JS_ENGINE, JS_ENDPOINT_NAME));
        endpoint = new Endpoint(wrapper, JS_ENDPOINT_NAME);
        endpoint.setExecutor(executor);
        when(executor.execute(input, CONTENT_TYPE.toString())).thenReturn(result);
    }

    @Test
    public void executeSetsMetadataOnEndpointResult() {
        EndPointResult executionResult = endpoint.execute(input, CONTENT_TYPE);
        assertEquals(metadata, executionResult.getInfo().get("ko"));
    }

    @Test
    public void executeReturnsResult() {
        EndPointResult executionResult = endpoint.execute(input, CONTENT_TYPE);
        assertEquals(result, executionResult.getResult());
    }


    @Test
    public void throwsErrorWithNullExecutor() {
        endpoint.setExecutor(null);
        ActivatorEndpointNotFoundException activatorException = assertThrows(ActivatorEndpointNotFoundException.class, () -> {
            endpoint.execute(input, CONTENT_TYPE);
        });
        assertEquals("No executor found for " + JS_ENDPOINT_ID, activatorException.getMessage());
    }

    @Test
    public void verifiesSupportedContentType() {
        assertTrue(endpoint.isSupportedContentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void verifiesNullContentType() {
        assertFalse(endpoint.isSupportedContentType(null));
    }

    @Test
    public void verifiesUnsupportedContentType() {
        assertFalse(endpoint.isSupportedContentType(MediaType.APPLICATION_XML));
    }

    @Test
    public void getSupportedContentTypesReturnsListOfTypes() {
        ArrayList typeList = new ArrayList();
        typeList.add(MediaType.APPLICATION_JSON_VALUE);
        assertEquals(typeList, endpoint.getSupportedContentTypes());
    }
}
package org.kgrid.activator.domain;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kgrid.activator.EndPointResult;
import org.kgrid.activator.domain.Endpoint;
import org.kgrid.activator.exceptions.ActivatorEndpointNotFoundException;
import org.kgrid.adapter.api.Executor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.kgrid.activator.testUtilities.KoCreationTestHelper.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Endpoint Tests")
public class EndpointTest {

    @Mock
    Executor executor;

    Endpoint endpoint;

    private final String input = "input";
    private final String result = "result";
    private final JsonNode metadata = generateMetadata(JS_NAAN, JS_NAME, JS_VERSION);

    @BeforeEach
    public void setUp() {
        endpoint = getEndpointForEngine(JS_ENGINE);
        endpoint.setExecutor(executor);
    }

    @Test
    @DisplayName("Execute returns Endpoint Result with metadata and output")
    public void executeSetsMetadataOnEndpointResult() {
        when(executor.execute(input, CONTENT_TYPE.toString())).thenReturn(result);
        EndPointResult executionResult = endpoint.execute(input, CONTENT_TYPE);
        assertAll(
                () -> assertEquals(metadata, executionResult.getInfo().get("ko")),
                () -> assertEquals(result, executionResult.getResult())
        );
    }

    @Test
    @DisplayName("Execute throws if executor is null")
    public void throwsErrorWithNullExecutor() {
        endpoint.setExecutor(null);
        ActivatorEndpointNotFoundException activatorException = assertThrows(ActivatorEndpointNotFoundException.class, () -> {
            endpoint.execute(input, CONTENT_TYPE);
        });
        assertEquals("No executor found for " + JS_ENDPOINT_ID, activatorException.getMessage());
    }

    @Test
    @DisplayName("Is supported Content Type returns true for good content type")
    public void verifiesSupportedContentType() {
        assertTrue(endpoint.isSupportedContentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Is supported Content Type returns false for null content type")
    public void verifiesNullContentType() {
        assertFalse(endpoint.isSupportedContentType(null));
    }

    @Test
    @DisplayName("Is supported Content Type returns false for bad content type")
    public void verifiesUnsupportedContentType() {
        assertFalse(endpoint.isSupportedContentType(MediaType.APPLICATION_XML));
    }

    @Test
    @DisplayName("get supported Content Types returns list of all supported types")
    public void getSupportedContentTypesReturnsListOfTypes() {
        ArrayList typeList = new ArrayList();
        typeList.add(MediaType.APPLICATION_JSON_VALUE);
        assertEquals(typeList, endpoint.getSupportedContentTypes());
    }
}
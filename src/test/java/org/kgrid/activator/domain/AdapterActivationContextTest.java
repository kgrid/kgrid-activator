package org.kgrid.activator.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.kgrid.activator.testUtilities.KoCreationTestHelper.JS_ENDPOINT_ID;
import static org.kgrid.activator.testUtilities.KoCreationTestHelper.JS_ENDPOINT_URI;
import static org.kgrid.activator.testUtilities.KoCreationTestHelper.JS_ENGINE;
import static org.kgrid.activator.testUtilities.KoCreationTestHelper.getEndpointForEngine;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kgrid.activator.exceptions.ActivatorEndpointNotFoundException;
import org.kgrid.activator.services.ActivationService;
import org.kgrid.adapter.api.ClientRequest;
import org.kgrid.adapter.api.ClientRequestBuilder;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.repository.CompoundDigitalObjectStore;
import org.springframework.core.env.Environment;

class AdapterActivationContextTest {
    private final Environment environment = mock(Environment.class);
    private final CompoundDigitalObjectStore cdoStore = mock(CompoundDigitalObjectStore.class);
    private AdapterActivationContext adapterActivationContext;
    private ActivationService activationService = mock(ActivationService.class);
    private final Map<URI, Endpoint> endpoints = new HashMap<>();
    private final Endpoint jsEndpoint = getEndpointForEngine(JS_ENGINE);
    private final String EXECUTOR_RESULT = "executed";

    @BeforeEach
    public void setup() {
        jsEndpoint.setExecutor(new Executor() {
            @Override
            public Object execute(ClientRequest r) {
                return EXECUTOR_RESULT;
            }
        });
        endpoints.put(JS_ENDPOINT_URI, jsEndpoint);
        when(activationService.getEndpoint(JS_ENDPOINT_URI)).thenReturn(jsEndpoint);
        adapterActivationContext = new AdapterActivationContext(environment, cdoStore, activationService);
    }

    @Test
    @DisplayName("Get Executor gets proper executor from endpoint map")
    public void getExecutorReturnsExecutor() {
        Executor executor = adapterActivationContext.getExecutor(JS_ENDPOINT_ID);
        assertEquals(jsEndpoint.getExecutor(), executor);
        assertEquals(EXECUTOR_RESULT, executor.execute(new ClientRequestBuilder().build()));
    }

    @Test
    @DisplayName("Get executor throws AdapterException for missing endpoint")
    public void getExecutorThrowsAdapterExceptionForMissingEndpoint() {
        when(activationService.getEndpoint(URI.create("a/b/c")))
            .thenThrow(new ActivatorEndpointNotFoundException(("seriously?")));
        ActivatorEndpointNotFoundException e =
            assertThrows(ActivatorEndpointNotFoundException.class,
            () -> adapterActivationContext.getExecutor("a/b/c")
        );
        assertTrue(e.getMessage().startsWith("seriously?"));
    }
    @Test
    @DisplayName("Get executor returns for missing executor")
    public void getExecutorReturnsNullForMissingExecutor() {
        jsEndpoint.setExecutor(null);
        Executor executor = adapterActivationContext.getExecutor(JS_ENDPOINT_ID);
        assertNull(executor);
    }

    @Test
    @DisplayName("Get property returns property from environment")
    public void getPropertyReturnsPropertyFromEnvironment() {
        String property = "inertia";
        String value = "is a property of matter";
        when(environment.getProperty(property)).thenReturn(value);
        String actualValue = adapterActivationContext.getProperty(property);
        assertAll(
                () -> verify(environment).getProperty(property),
                () -> assertEquals(value, actualValue)
        );
    }

    @Test
    @DisplayName("Get Binary gets binary from cdo store")
    public void getBinaryReturnsBinaryFromCdoStore() {
        when(cdoStore.getBinaryStream(JS_ENDPOINT_URI)).thenReturn(null);
        adapterActivationContext.getBinary(JS_ENDPOINT_URI);
        verify(cdoStore).getBinaryStream(JS_ENDPOINT_URI);
    }
}

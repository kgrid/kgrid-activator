package org.kgrid.activator.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kgrid.activator.services.ActivationService;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.repository.CompoundDigitalObjectStore;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.kgrid.activator.testUtilities.KoCreationTestHelper.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdapterActivationContextTest {
    private final Environment environment = Mockito.mock(Environment.class);
    private final CompoundDigitalObjectStore cdoStore = Mockito.mock(CompoundDigitalObjectStore.class);
    private AdapterActivationContext adapterActivationContext;
    private ActivationService activationService = Mockito.mock(ActivationService.class);
    private final Map<URI, Endpoint> endpoints = new HashMap<>();
    private final Endpoint jsEndpoint = getEndpointForEngine(JS_ENGINE);
    private final String EXECUTOR_RESULT = "executed";

    @BeforeEach
    public void setup() {
        jsEndpoint.setExecutor((o, s) -> EXECUTOR_RESULT);
        endpoints.put(JS_ENDPOINT_URI, jsEndpoint);
        when(activationService.getEndpointMap()).thenReturn(endpoints);
        adapterActivationContext = new AdapterActivationContext(environment, cdoStore, activationService);
    }

    @Test
    @DisplayName("Get Executor gets proper executor from endpoint map")
    public void getExecutorReturnsExecutor() {
        Executor executor = adapterActivationContext.getExecutor(JS_ENDPOINT_ID);
        assertEquals(EXECUTOR_RESULT, executor.execute(null, null));
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

    @Test
    @DisplayName("Activation Context get executor throws if endpoint is not in endpoint map")
    public void loadAndInitialize_ActivationContextGetExecutorThrowsIfEndpointIsNotInEndpointMap() {
        endpoints.clear();

        AdapterException adapterException = assertThrows(AdapterException.class,
                () -> adapterActivationContext.getExecutor(JS_ENDPOINT_URI.toString()));
        assertEquals(
                String.format("Can't find executor in app context for endpoint %s", JS_ENDPOINT_ID),
                adapterException.getMessage());
    }
}
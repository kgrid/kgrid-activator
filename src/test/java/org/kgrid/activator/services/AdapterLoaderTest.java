package org.kgrid.activator.services;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kgrid.adapter.api.ActivationContext;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.Executor;
import org.kgrid.mock.adapter.MockAdapter;
import org.kgrid.shelf.repository.CompoundDigitalObjectStore;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthContributorRegistry;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.env.Environment;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.kgrid.activator.testUtilities.KoCreationTestHelper.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Adapter Loader Tests")
public class AdapterLoaderTest {
    public static final String MOCK_ADAPTER_ENGINE = "mockadapter";
    @Mock
    private AutowireCapableBeanFactory beanFactory;
    @Mock
    private Environment environment;
    @Mock
    private CompoundDigitalObjectStore cdoStore;
    @Mock
    private HealthContributorRegistry registry;
    @InjectMocks
    private AdapterLoader adapterLoader;

    private final Map<URI, Endpoint> endpoints = new HashMap<>();
    private final Endpoint jsEndpoint = getEndpointForEngine(JS_ENGINE);
    private final String EXECUTOR_RESULT = "executed";

    @BeforeEach
    public void setup() {
        jsEndpoint.setExecutor((o, s) -> EXECUTOR_RESULT);
        endpoints.put(JS_ENDPOINT_URI, jsEndpoint);
    }

    @Test
    @DisplayName("Load and initialize returns Adapter Resolver with all Adapters")
    public void loadAndInitialize_returnsAdapterResolver() {
        AdapterResolver adapterResolver = adapterLoader.loadAndInitializeAdapters(endpoints);
        assertAll(
                () -> verify(beanFactory, times(4)).autowireBean(any()),
                () -> assertNotNull(adapterResolver),
                () -> assertEquals(adapterResolver.getAdapter(MOCK_ADAPTER_ENGINE).status(), "UP")
        );
    }

    @Test
    @DisplayName("Load and initialize registers health endpoint for adapters")
    public void loadAndInitialize_registersHealthEndpointForAdapter() {
        adapterLoader.loadAndInitializeAdapters(endpoints);
        ArgumentCaptor<HealthIndicator> healthIndicatorArgumentCaptor = ArgumentCaptor.forClass(HealthIndicator.class);
        verify(registry).registerContributor(
                eq(MockAdapter.class.getName()), healthIndicatorArgumentCaptor.capture());
        HealthIndicator healthIndicator = healthIndicatorArgumentCaptor.getValue();
        Health health = healthIndicator.getHealth(true);
        Collection engines = (Collection) health.getDetails().get("engines");
        assertTrue(engines.contains(MOCK_ADAPTER_ENGINE));
    }

    @Test
    @DisplayName("Load and initialize does not throw if health endpoint registration fails")
    public void loadAndInitialize_DoesNotThrowIfRegisteringHealthFails() {
        doThrow(new IllegalStateException()).when(registry).registerContributor(any(), any());
        adapterLoader.loadAndInitializeAdapters(endpoints);
    }

    @Test
    @DisplayName("Load and initialize sets executor on activation context")
    public void loadAndInitialize_SetsExecutorOnActivationContext() {
        ActivationContext activationContext = loadAndInitializeAndGetActivationContext();
        Executor executor = activationContext.getExecutor(JS_ENDPOINT_URI.toString());
        assertEquals(EXECUTOR_RESULT, executor.execute(null, null));
    }

    @Test
    @DisplayName("Activation Context get executor throws if endpoint is not in endpoint map")
    public void loadAndInitialize_ActivationContextGetExecutorThrowsIfEndpointIsNotInEndpointMap() {
        endpoints.clear();
        ActivationContext activationContext = loadAndInitializeAndGetActivationContext();

        AdapterException adapterException = assertThrows(AdapterException.class,
                () -> activationContext.getExecutor(JS_ENDPOINT_URI.toString()));
        assertEquals(
                String.format("Can't find executor in app context for endpoint %s", JS_ENDPOINT_ID),
                adapterException.getMessage());
    }


    @Test
    @DisplayName("Load and initialize sets cdo store on activation context")
    public void loadAndInitialize_SetsCdoStoreOnActivationContext() {
        when(cdoStore.getBinaryStream(JS_ENDPOINT_URI)).thenReturn(null);

        ActivationContext activationContext = loadAndInitializeAndGetActivationContext();
        activationContext.getBinary(JS_ENDPOINT_URI);
        verify(cdoStore).getBinaryStream(JS_ENDPOINT_URI);
    }

    @Test
    @DisplayName("Load and initialize sets environment on activation context")
    public void loadAndInitialize_SetsEnvironmentOnActivationContext() {
        ActivationContext activationContext = loadAndInitializeAndGetActivationContext();
        String property = "bonkers";
        activationContext.getProperty(property);
        verify(environment).getProperty(property);
    }

    private ActivationContext loadAndInitializeAndGetActivationContext() {
        AdapterResolver adapterResolver = adapterLoader.loadAndInitializeAdapters(endpoints);
        MockAdapter mockAdapter = (MockAdapter) adapterResolver.getAdapter(MOCK_ADAPTER_ENGINE);
        return mockAdapter.getActivationContext();
    }
}

package org.kgrid.activator.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kgrid.activator.domain.Endpoint;
import org.kgrid.adapter.api.*;
import org.kgrid.mock.adapter.MockAdapter;
import org.kgrid.shelf.repository.CompoundDigitalObjectStore;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthContributorRegistry;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.kgrid.activator.testUtilities.KoCreationTestHelper.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    @Mock
    ActivationService activationService;
    @Spy
    private final ApplicationContext applicationContext = new ClassPathXmlApplicationContext();
    @InjectMocks
    private AdapterLoader adapterLoader;

    private final Map<URI, Endpoint> endpointMap = new TreeMap<>();

    private static final String EXECUTOR_RESULT = "executed";

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(adapterLoader, "adapterLocations", new String[]{"file:src/test/resources/adapters"});
        Endpoint jsEndpoint = getEndpointForEngine(JS_ENGINE);
        requireNonNull(jsEndpoint).setExecutor(new Executor() {
            @Override
            public ExecutorResponse execute(ClientRequest r) {
                return new ExecutorResponse(EXECUTOR_RESULT, null);
            }
        });

        endpointMap.put(JS_ENDPOINT_URI, jsEndpoint);
        Mockito.lenient().when(activationService.getEndpoint(JS_ENDPOINT_URI)).thenReturn(jsEndpoint);
    }

    @Test
    @DisplayName("Load and initialize returns Adapter Resolver with all Adapters")
    public void loadAndInitialize_returnsListOfAdapters() {
        List<Adapter> adapters = adapterLoader.loadAdapters();
        adapterLoader.initializeAdapters(adapters);
        assertAll(
                () -> verify(beanFactory, times(5)).autowireBean(any()),
                () -> assertNotNull(adapters),
                () -> assertEquals("UP", adapters.get(0).status())
        );
    }

    @Test
    @DisplayName("Load and initialize registers health endpoint for adapters")
    public void loadAndInitialize_registersHealthEndpointForAdapter() {
        List<Adapter> adapters = adapterLoader.loadAdapters();
        adapterLoader.initializeAdapters(adapters);
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
        adapterLoader.loadAdapters();
    }

    @Test
    @DisplayName("Load and initialize sets executor on activation context")
    public void loadAndInitialize_SetsExecutorOnActivationContext() {
        ActivationContext activationContext = loadAndInitializeAndGetActivationContext();
        Executor executor = activationContext.getExecutor(JS_ENDPOINT_URI.toString());
        assertEquals(EXECUTOR_RESULT, executor.execute(new ClientRequest.Builder().build()).getBody());
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
        List<Adapter> adapters = adapterLoader.loadAdapters();
        adapterLoader.initializeAdapters(adapters);
        MockAdapter mockAdapter = (MockAdapter) adapters.get(0);
        return mockAdapter.getActivationContext();
    }
}

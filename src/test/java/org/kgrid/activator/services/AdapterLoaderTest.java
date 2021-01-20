package org.kgrid.activator.services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.kgrid.adapter.api.ActivationContext;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.Executor;
import org.kgrid.mock.adapter.MockAdapter;
import org.kgrid.shelf.domain.KnowledgeObjectWrapper;
import org.kgrid.shelf.repository.CompoundDigitalObjectStore;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthContributorRegistry;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.env.Environment;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.kgrid.activator.utils.KoCreationTestHelper.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
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
  private final URI endpointUri = URI.create(String.format("%s/%s/%s/%s", NAAN, NAME, API_VERSION, ENDPOINT_NAME));
  private final KnowledgeObjectWrapper kow = new KnowledgeObjectWrapper(generateMetadata(NAAN,NAME,VERSION));
  private final Endpoint endpoint = new Endpoint(
          kow, ENDPOINT_NAME);
  private final String EXECUTOR_RESULT = "executed";

  @Before
  public void setup() {

    endpoint.setExecutor((o, s) -> EXECUTOR_RESULT);
    endpoints.put(endpointUri, endpoint);
    when(cdoStore.getBinaryStream(endpointUri)).thenReturn(null);
  }

  @Test
  public void loadAndInitialize_returnsAdapterResolver() {
    AdapterResolver adapterResolver = adapterLoader.loadAndInitializeAdapters(endpoints);
    assertNotNull(adapterResolver);
  }

  @Test
  public void loadAndInitialize_autowiresAdapterBeans() {
    adapterLoader.loadAndInitializeAdapters(endpoints);
    verify(beanFactory, times(4)).autowireBean(any());
  }

  @Test
  public void loadAndInitialize_initializesAdapter() {
    AdapterResolver adapterResolver = adapterLoader.loadAndInitializeAdapters(endpoints);
    Adapter mockAdapter = adapterResolver.getAdapter(MOCK_ADAPTER_ENGINE);
    assertEquals(mockAdapter.status(), "UP");
  }

  @Test
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
  public void loadAndInitialize_DoesNotThrowIfRegisteringHealthFails() {
    doThrow(new IllegalStateException()).when(registry).registerContributor(any(), any());
    adapterLoader.loadAndInitializeAdapters(endpoints);
  }

  @Test
  public void loadAndInitialize_SetsExecutorOnActivationContext() {
    AdapterResolver adapterResolver = adapterLoader.loadAndInitializeAdapters(endpoints);
    MockAdapter mockAdapter = (MockAdapter) adapterResolver.getAdapter(MOCK_ADAPTER_ENGINE);
    ActivationContext activationContext = mockAdapter.getActivationContext();
    Executor executor = activationContext.getExecutor(endpointUri.toString());
    assertEquals(EXECUTOR_RESULT, executor.execute(null, null));
  }

  @Test
  public void loadAndInitialize_ActivationContextGetExecutorThrowsIfEndpointIsNotInEndpointMap() {
    endpoints.clear();
    AdapterResolver adapterResolver = adapterLoader.loadAndInitializeAdapters(endpoints);
    MockAdapter mockAdapter = (MockAdapter) adapterResolver.getAdapter(MOCK_ADAPTER_ENGINE);
    ActivationContext activationContext = mockAdapter.getActivationContext();

    AdapterException adapterException = Assert.assertThrows(AdapterException.class,
            () -> activationContext.getExecutor(endpointUri.toString()));
    Assertions.assertEquals(
            "Can't find executor in app context for endpoint naan/name/ApiVersion/endpoint",
            adapterException.getMessage());
  }

  @Test
  public void loadAndInitialize_SetsCdoStoreOnActivationContext() {
    AdapterResolver adapterResolver = adapterLoader.loadAndInitializeAdapters(endpoints);
    MockAdapter mockAdapter = (MockAdapter) adapterResolver.getAdapter(MOCK_ADAPTER_ENGINE);
    ActivationContext activationContext = mockAdapter.getActivationContext();
    activationContext.getBinary(endpointUri);
    verify(cdoStore).getBinaryStream(endpointUri);
  }

  @Test
  public void loadAndInitialize_SetsEnvironmentOnActivationContext() {
    AdapterResolver adapterResolver = adapterLoader.loadAndInitializeAdapters(endpoints);
    MockAdapter mockAdapter = (MockAdapter) adapterResolver.getAdapter(MOCK_ADAPTER_ENGINE);
    ActivationContext activationContext = mockAdapter.getActivationContext();
    String property = "bonkers";
    activationContext.getProperty(property);
    verify(environment).getProperty(property);
  }
}

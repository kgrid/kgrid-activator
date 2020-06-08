package org.kgrid.activator.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kgrid.mock.adapter.MockAdapter;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthContributorRegistry;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AdapterLoaderHealthEndpointTest {

  @Mock
  AutowireCapableBeanFactory beanFactory;
  @Mock HealthContributorRegistry healthContributorRegistry;
  @InjectMocks AdapterLoader adapterLoader;

  @Test
  public void testCreatesHealthEndpointsForAdapters() {
    MockAdapter mockAdapter = new MockAdapter();
    mockAdapter.initialize(null);
    ArgumentCaptor<HealthIndicator> healthIndicatorArgumentCaptor =
        ArgumentCaptor.forClass(HealthIndicator.class);

    adapterLoader.loadAndInitializeAdapters(Collections.EMPTY_MAP);

    verify(healthContributorRegistry, times(1))
        .registerContributor(
            eq("org.kgrid.mock.adapter.MockAdapter"), healthIndicatorArgumentCaptor.capture());
    HealthIndicator healthIndicator = healthIndicatorArgumentCaptor.getValue();

    Health health = healthIndicator.getHealth(true);
    assertEquals(mockAdapter.status(), health.getStatus().toString());
    assertEquals(mockAdapter.getType(), health.getDetails().get("type"));
    assertNotNull(health.getDetails().get("created"));
  }
}

package org.kgrid.activator.services;

import org.kgrid.activator.domain.AdapterActivationContext;
import org.kgrid.activator.domain.Endpoint;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.shelf.repository.CompoundDigitalObjectStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthContributorRegistry;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

@Service
public class AdapterLoader {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final AutowireCapableBeanFactory beanFactory;
    private final Environment environment;
    private final CompoundDigitalObjectStore cdoStore;
    private final HealthContributorRegistry registry;
    private final Map<URI, Endpoint> endpointMap;

    public AdapterLoader(AutowireCapableBeanFactory beanFactory, Environment environment,
                         CompoundDigitalObjectStore cdoStore, HealthContributorRegistry registry,
                         Map<URI, Endpoint> endpointMap) {
        this.beanFactory = beanFactory;
        this.environment = environment;
        this.cdoStore = cdoStore;
        this.registry = registry;
        this.endpointMap = endpointMap;
    }

    public AdapterResolver loadAndInitializeAdapters() {

        final List<Adapter> adapters = new ArrayList<>();
        ServiceLoader<Adapter> loader = ServiceLoader.load(Adapter.class);
        loader.forEach(adapter -> {
            beanFactory.autowireBean(adapter);
            adapter.initialize(new AdapterActivationContext(endpointMap, environment, cdoStore));
            adapters.add(adapter);
            registerHealthEndpoint(adapter);
        });
        return new AdapterResolver(adapters);
    }

    private void registerHealthEndpoint(Adapter adapter) {
        HealthIndicator indicator = () ->
                Health.status(adapter.status())
                        .withDetail("engines", adapter.getEngines())
                        .build();
        try {
            registry.registerContributor(adapter.getClass().getName(), indicator);
        } catch (IllegalStateException e) {
            log.info(e.getMessage());
        }
    }
}

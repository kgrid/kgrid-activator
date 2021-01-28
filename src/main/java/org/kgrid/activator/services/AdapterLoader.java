package org.kgrid.activator.services;

import org.kgrid.activator.domain.AdapterActivationContext;
import org.kgrid.activator.domain.Endpoint;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.shelf.repository.CompoundDigitalObjectStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private AutowireCapableBeanFactory beanFactory;
    @Autowired
    private Environment environment;
    @Autowired
    private CompoundDigitalObjectStore cdoStore;
    @Autowired
    private HealthContributorRegistry registry;

    public AdapterResolver loadAndInitializeAdapters(Map<URI, Endpoint> endpoints) {

        List<Adapter> adapters = new ArrayList<>();
        ServiceLoader<Adapter> loader = ServiceLoader.load(Adapter.class);
        for (Adapter adapter : loader) {
            beanFactory.autowireBean(adapter);
            adapter.initialize(new AdapterActivationContext(
                    endpoints, environment, cdoStore));
            adapters.add(adapter);
            registerHealthEndpoint(adapter);
        }
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

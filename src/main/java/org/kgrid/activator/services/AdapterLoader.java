package org.kgrid.activator.services;

import org.kgrid.activator.domain.AdapterActivationContext;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.shelf.repository.CompoundDigitalObjectStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthContributorRegistry;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

@Service
@ComponentScan(basePackages = "org.kgrid.adapter")
public class AdapterLoader {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final AutowireCapableBeanFactory beanFactory;
    private final HealthContributorRegistry registry;
    private final Environment environment;
    private final CompoundDigitalObjectStore cdoStore;
    private final ActivationService activationService;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    public AdapterLoader(DefaultListableBeanFactory beanFactory, HealthContributorRegistry registry, Environment environment,
                         CompoundDigitalObjectStore cdoStore, ActivationService activationService, RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.beanFactory = beanFactory;
        this.registry = registry;
        this.environment = environment;
        this.cdoStore = cdoStore;
        this.activationService = activationService;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    public List<Adapter> loadAdapters() {
        final List<Adapter> adapters = new ArrayList<>();
        ServiceLoader<Adapter> loader = ServiceLoader.load(Adapter.class);
        loader.forEach(adapter -> {
            beanFactory.autowireBean(adapter);
            registerHealthEndpoint(adapter);
            adapters.add(adapter);
        });
        return adapters;
    }

    public void initializeAdapters(List<Adapter> adapters) {
        adapters.forEach(adapter -> adapter.initialize(new AdapterActivationContext(environment, cdoStore, activationService)));
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

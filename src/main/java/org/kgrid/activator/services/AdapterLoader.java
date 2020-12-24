package org.kgrid.activator.services;

import org.kgrid.adapter.api.ActivationContext;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.Executor;
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

import java.io.InputStream;
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

    private static AdapterResolver resolver;

    public AdapterResolver loadAndInitializeAdapters(Map<URI, Endpoint> endpoints) {

        List<Adapter> adapters = new ArrayList<>();
        ServiceLoader<Adapter> loader = ServiceLoader.load(Adapter.class);
        for (Adapter adapter : loader) {
            beanFactory.autowireBean(adapter);
            initializeAdapter(adapter, endpoints);
            adapters.add(adapter);
            registerHealthEndpoint(adapter);
        }
        resolver = new AdapterResolver(adapters);
        return resolver;
    }

    private void registerHealthEndpoint(Adapter adapter) {
        HealthIndicator types = () ->
                Health.status(adapter.status())
                        .withDetail("types", adapter.getEngines())
                        .build();
        HealthIndicator indicator =
                types;
        try {
            registry.registerContributor(adapter.getClass().getName(), indicator);
        } catch (IllegalStateException e) {
            log.info(e.getMessage());
        }
    }

    private void initializeAdapter(Adapter adapter, Map<URI, Endpoint> endpoints) {
        ActivationContext activationContext = new ActivationContext() {
            @Override
            public Executor getExecutor(String key) {
                URI id = URI.create(key);
                if (endpoints.containsKey(id)) {
                    return endpoints.get(id).getExecutor();
                } else {
                    String message = String.format("Can't find executor in app context for endpoint %s.", key);
                    log.error(message);
                    throw new AdapterException(message);
                }
            }

            @Override
            public InputStream getBinary(URI pathToBinary) {
                return cdoStore.getBinaryStream(pathToBinary);
            }

            @Override
            public String getProperty(String key) {
                return environment.getProperty(key);
            }
        };
        adapter.initialize(activationContext);
    }
}

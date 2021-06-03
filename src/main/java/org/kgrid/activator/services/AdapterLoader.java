package org.kgrid.activator.services;

import org.kgrid.activator.domain.AdapterActivationContext;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.shelf.repository.CompoundDigitalObjectStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthContributorRegistry;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

@Service
@ComponentScan(basePackages = "org.kgrid.adapter")
public class AdapterLoader {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Value("${kgrid.activator.adapter-locations:file:adapters}")
    String[] adapterLocations;

    private final AutowireCapableBeanFactory beanFactory;
    private final HealthContributorRegistry registry;
    private final Environment environment;
    private final CompoundDigitalObjectStore cdoStore;
    private final ActivationService activationService;
    private final ApplicationContext applicationContext;

    public AdapterLoader(AutowireCapableBeanFactory beanFactory, HealthContributorRegistry registry, Environment environment,
                         CompoundDigitalObjectStore cdoStore, ActivationService activationService, ApplicationContext applicationContext) {
        this.beanFactory = beanFactory;
        this.registry = registry;
        this.environment = environment;
        this.cdoStore = cdoStore;
        this.activationService = activationService;
        this.applicationContext = applicationContext;
    }

    public List<Adapter> loadAdapters() {
        ArrayList<URL> adapterUrls = new ArrayList<>();
        for (String location : adapterLocations) {
            Resource adapterSource = applicationContext.getResource(location);
            if (!adapterSource.exists()) {
                log.warn("Cannot load external adapter from location {}, it does not exist", adapterSource.getDescription());
            } else {
                log.info("Loading external adapter {}", adapterSource.getDescription());
            }
            try {
                if (!adapterSource.isFile() || !adapterSource.getFile().isDirectory()) {
                    adapterUrls.add(new URL("jar:" + adapterSource.getURL() + "!/"));
                } else {
                    File adapterFile = adapterSource.getFile();
                    adapterUrls.addAll(Arrays.stream(Objects.requireNonNull(adapterFile.listFiles((file -> file.getName().endsWith(".jar"))))).map(file -> {
                        try {
                            return new URL("jar:file:" + file + "!/");
                        } catch (MalformedURLException e) {
                            log.warn("Cannot load adapter for file {}, cause: {}", file.getName(), e.getMessage());
                            return null;
                        }
                    }).filter(Objects::nonNull).collect(Collectors.toList()));
                }
            } catch (IOException e) {
                log.warn("Cannot load adapter for file {}, cause: {}", adapterSource.getFilename(), e.getMessage());
            }
        }
        final List<Adapter> adapters = new ArrayList<>();
        ClassLoader classLoader = new URLClassLoader(adapterUrls.toArray(new URL[0]), Adapter.class.getClassLoader());
        ServiceLoader<Adapter> dropInLoader = ServiceLoader.load(Adapter.class, classLoader);
        dropInLoader.forEach(adapter -> registerAdapter(adapters, adapter));

        return adapters;
    }

    private void registerAdapter(List<Adapter> adapters, Adapter adapter) {
        beanFactory.autowireBean(adapter);
        registerHealthEndpoint(adapter);
        adapters.add(adapter);
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

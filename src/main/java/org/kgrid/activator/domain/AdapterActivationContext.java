package org.kgrid.activator.domain;

import org.kgrid.activator.services.ActivationService;
import org.kgrid.adapter.api.ActivationContext;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.repository.CompoundDigitalObjectStore;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import java.io.InputStream;
import java.net.URI;

public class AdapterActivationContext implements ActivationContext {
    private final Environment environment;
    private final CompoundDigitalObjectStore cdoStore;
    private final ActivationService activationService;

    public AdapterActivationContext(Environment environment, CompoundDigitalObjectStore cdoStore,
                                    ActivationService activationService) {
        this.environment = environment;
        this.cdoStore = cdoStore;
        this.activationService = activationService;
    }

    @Override
    public Executor getExecutor(String key) {
        final Endpoint endpoint = activationService.getEndpoint(URI.create(key));
        return endpoint.getExecutor();
    }

    @Override
    public InputStream getBinary(URI pathToBinary) {
        return cdoStore.getBinaryStream(pathToBinary);
    }

    @Override
    public String getProperty(String key) {
        return environment.getProperty(key);
    }

    @Override
    @Async
    public void refresh(String engineName) {
        activationService.activateForEngine(engineName);
    }
}

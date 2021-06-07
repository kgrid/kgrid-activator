package org.kgrid.activator.domain;

import org.kgrid.activator.services.ActivationService;
import org.kgrid.adapter.api.ActivationContext;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.repository.CompoundDigitalObjectStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import java.io.InputStream;
import java.net.URI;

public class AdapterActivationContext implements ActivationContext {
    private final Environment environment;
    private final CompoundDigitalObjectStore cdoStore;
    private final ActivationService activationService;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public AdapterActivationContext(Environment environment, CompoundDigitalObjectStore cdoStore,
                                    ActivationService activationService) {
        this.environment = environment;
        this.cdoStore = cdoStore;
        this.activationService = activationService;
    }

    @Override
    public Executor getExecutor(String key) {
        URI id = URI.create(key);
//        final Map<URI, Endpoint> endpointMap = activationService.getEndpointMap();
        Endpoint ep = activationService.getEndpoint(id);
        if (ep != null) {
            return ep.getExecutor();
        } else {
            String message = String.format("Can't find executor in app context for endpoint %s", key);
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

    @Override
    @Async
    public void refresh(String engineName) {
        activationService.activateForEngine(engineName);
    }
}

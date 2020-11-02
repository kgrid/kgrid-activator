package org.kgrid.activator;

import org.apache.commons.lang3.StringUtils;
import org.kgrid.activator.controller.ActivationController;
import org.kgrid.activator.services.AdapterLoader;
import org.kgrid.activator.services.AdapterResolver;
import org.kgrid.activator.services.Endpoint;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.repository.CompoundDigitalObjectStore;
import org.kgrid.shelf.repository.CompoundDigitalObjectStoreFactory;
import org.kgrid.shelf.repository.FilesystemCDOWatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.CrossOrigin;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;

@SpringBootApplication(scanBasePackages = {"org.kgrid.shelf", "org.kgrid.activator", "org.kgrid.adapter"})
@EnableSwagger2
@CrossOrigin
public class KgridActivatorApplication implements CommandLineRunner {

    @Autowired
    private ActivationController activationController;

    public static void main(String[] args) {
        new SpringApplicationBuilder(KgridActivatorApplication.class)
                .build()
                .run(args);
    }

    @Primary
    @Bean
    public static CompoundDigitalObjectStore getCDOStore(
            @Value("${kgrid.shelf.cdostore.url:filesystem:file://shelf}") String cdoStoreURI) {
        cdoStoreURI = cdoStoreURI.replace(" ","%20");
        return CompoundDigitalObjectStoreFactory.create(cdoStoreURI);
    }

    @Bean
    public static AdapterResolver getAdapterResolver(AdapterLoader loader,
                                                     Map<URI, Endpoint> endpoints) {
        return loader.loadAndInitializeAdapters(endpoints);
    }

    @Bean
    public static Map<URI, Endpoint> getEndpoints() {
        return new HashMap<>();
    }

    @Override
    public void run(String... strings) {
        activationController.activate();
    }
}

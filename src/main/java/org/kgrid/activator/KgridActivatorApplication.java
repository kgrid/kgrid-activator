package org.kgrid.activator;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.kgrid.activator.services.ActivationService;
import org.kgrid.activator.services.AdapterLoader;
import org.kgrid.activator.services.AdapterResolver;
import org.kgrid.activator.services.Endpoint;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.repository.CompoundDigitalObjectStore;
import org.kgrid.shelf.repository.CompoundDigitalObjectStoreFactory;
import org.kgrid.shelf.repository.FilesystemCDOWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.CrossOrigin;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication(scanBasePackages = {"org.kgrid.shelf", "org.kgrid.activator"})
@EnableSwagger2
@CrossOrigin
public class KgridActivatorApplication implements CommandLineRunner {

  @Autowired
  private Map<String, Endpoint> endpoints;

  @Autowired
  private ActivationService activationService;

  @Autowired
  private EndpointLoader endpointLoader;

  private FilesystemCDOWatcher watcher;

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Value("${kgrid.shelf.cdostore.url:filesystem:file://shelf}")
  private String cdoStoreURI;

  public static void main(String[] args) {
    new SpringApplicationBuilder(KgridActivatorApplication.class)
        .build()
        .run(args);
  }

  @Primary
  @Bean
  public static CompoundDigitalObjectStore getCDOStore(
      @Value("${kgrid.shelf.cdostore.url:filesystem:file://shelf}") String cdoStoreURI) {
    return CompoundDigitalObjectStoreFactory.create(cdoStoreURI);
  }

  @Bean
  public static AdapterResolver getAdapterResolver(AdapterLoader loader,
      Map<String, Endpoint> endpoints) {
    return loader.loadAndInitializeAdapters(endpoints);
  }

  @Bean
  public static Map<String, Endpoint> getEndpoints() {
    return new HashMap<>();
  }

  @Override
  public void run(String... strings) throws Exception {
    endpoints.putAll(endpointLoader.load());
    activationService.activate(endpoints);
    this.watchShelf();
  }

  // *****************************************
  //Swagger API Documentation Generation
  // *****************************************
  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.basePackage("org.kgrid"))
        .paths(PathSelectors.any())
        .build();
  }


  // Reloads one object if that object has changed or was added
  // Removes an object if an entire object or implementation was deleted
  private void watchShelf() throws IOException {
    if (watcher != null) {
      return;
    }
    watcher = new FilesystemCDOWatcher();
    watcher.registerAll(Paths.get(endpointLoader.getKORepoLocation()),
        ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE);
    String cdoStoreFilePath = StringUtils.substringAfterLast(cdoStoreURI, ":");
    HashMap<ArkId, Long> lastModified = new HashMap<>();

    watcher.addFileListener((path, eventType) -> {
      String[] pathParts = StringUtils.split(path.toString().substring(cdoStoreFilePath.length() - 1), "/");
      ArkId arkId;
      if(pathParts.length > 1) {
        arkId = new ArkId(StringUtils.join(pathParts[0], "/", pathParts[1]));
      } else {
        arkId = new ArkId(pathParts[0]);
      }

      if (eventType == ENTRY_DELETE && path.toFile().isDirectory()) {
        endpoints.keySet().forEach(key -> {
          if(StringUtils.isNotEmpty(arkId.getImplementation())) {
            if (key.contains(arkId.getDashArk())) {
              endpoints.remove(key);
            }
          } else {
            if(key.contains(arkId.getDashArkImplementation())) {
              endpoints.remove(key);
            }
          }
        });
      } else if(StringUtils.isNotEmpty(arkId.getImplementation())){
        Map<String, Endpoint> newEndpoints = endpointLoader.load(arkId);
        endpoints.putAll(newEndpoints);
        activationService.activate(newEndpoints);
      }
    });
    new Thread(watcher).start();
  }

}

package org.kgrid.activator;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;
import org.kgrid.activator.endpoint.ActivateEndpoint;
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
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication(scanBasePackages = {"org.kgrid.shelf", "org.kgrid.activator", "org.kgrid.adapter"})
@EnableSwagger2
@CrossOrigin
public class KgridActivatorApplication implements CommandLineRunner {

  @Autowired
  private Map<URI, Endpoint> endpoints;

  @Autowired
  private ActivationService activationService;

  @Autowired
  private ActivateEndpoint activateEndpoint;

  @Autowired
  private EndpointLoader endpointLoader;

  private FilesystemCDOWatcher watcher;

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Value("${kgrid.shelf.cdostore.url:filesystem:file://shelf}")
  private String cdoStoreURI;

  @Value("${kgrid.activator.autoreload:false}")
  private String autoReload;

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
      Map<URI, Endpoint> endpoints) {
    return loader.loadAndInitializeAdapters(endpoints);
  }

  @Bean
  public static TreeMap<URI, Endpoint> getEndpoints() {
    return new TreeMap<>(Collections.reverseOrder());
  }

  @Override
  public void run(String... strings) throws Exception {
    activateEndpoint.activate();
    if(Boolean.valueOf(autoReload)) {
      this.watchShelf();
    }
  }

  // Reloads one object if that object has changed or was added
  // Removes an object if an entire object or implementation was deleted
  private void watchShelf() throws IOException {
    if (watcher != null) {
      return;
    }
    watcher = new FilesystemCDOWatcher();
    final URI koRepoLocation = endpointLoader.getKORepoLocation();
    watcher.registerAll(Paths.get(koRepoLocation),
        ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE);
    String cdoStoreFilePath = StringUtils.substringAfterLast(cdoStoreURI, ":");

    watcher.addFileListener((path, eventType) -> {
      String[] pathParts = StringUtils.split(path.toString().substring(cdoStoreFilePath.length() - 1), "/");
      ArkId arkId;
      if(pathParts.length > 1) {
        arkId = new ArkId(StringUtils.join(pathParts[0], "/", pathParts[1]));
      } else {
        arkId = new ArkId(pathParts[0]);
      }

      // With the new activation logic this now handles all modification cases
      // Throws an error on delete though, maybe could clean it up but problematic because
      // Delete is always preceded by a modify, could try a using a queue where events are paired
      // and the first one is held in case the second is a delete but this is simple and works for now
      activateEndpoint.activate(arkId);

    });
    new Thread(watcher).start();
  }

}

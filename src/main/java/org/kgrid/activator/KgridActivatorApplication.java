package org.kgrid.activator;

import java.util.Map;
import org.kgrid.activator.services.ActivationService;
import org.kgrid.activator.services.AdapterService;
import org.kgrid.activator.services.Endpoint;
import org.kgrid.shelf.repository.CompoundDigitalObjectStore;
import org.kgrid.shelf.repository.CompoundDigitalObjectStoreFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication(scanBasePackages = {"org.kgrid.shelf", "org.kgrid.activator"})
@EnableSwagger2
public class KgridActivatorApplication implements CommandLineRunner {

  @Autowired
  private ActivationService activationService;
  @Autowired
  private AdapterService adapterService;

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

  @Override
  public void run(String... strings) throws Exception {
    Map<String, Endpoint> eps = activationService.loadEndpoints();
    adapterService.loadAndInitializeAdapters();
    activationService.activate(eps);
    adapterService.loadAndInitializeAdapters();
//    activationService.startEndpointWatcher();
  }

  //Swagger API Documentation Generation
  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.basePackage("org.kgrid"))
        .paths(PathSelectors.any())
        .build();
  }

}

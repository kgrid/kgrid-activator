package org.kgrid.activator;

import org.kgrid.activator.services.ActivationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication(scanBasePackages = { "org.kgrid.shelf", "org.kgrid.activator"})
@EnableSwagger2
public class KgridActivatorApplication implements CommandLineRunner {

	@Autowired
	private ActivationService service;
	public static void main(String[] args) {

		new SpringApplicationBuilder(KgridActivatorApplication.class)
				.build()
				.run(args);

	}

	@Override
	public void run(String... strings) throws Exception {
			service.loadAndInitializeAdapters();
			service.loadAndActivateEndpoints();
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

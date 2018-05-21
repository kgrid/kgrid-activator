package org.kgrid.activator;

import org.kgrid.activator.services.ActivationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = { "org.kgrid.shelf.domain", "org.kgrid.shelf.repository", "org.kgrid.activator"})
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
}

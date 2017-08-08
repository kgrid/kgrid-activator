package edu.umich.lhs.activator;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class KgridActivatorApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {

		new SpringApplicationBuilder(KgridActivatorApplication.class)
				.build()
				.run(args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application
				.sources(KgridActivatorApplication.class);
	}

}

package org.uofm.ot.executionStack;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class ObjectTellerExecutionStackApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {

		new SpringApplicationBuilder(ObjectTellerExecutionStackApplication.class)
				.build()
				.run(args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application
				.sources(ObjectTellerExecutionStackApplication.class);
	}


}

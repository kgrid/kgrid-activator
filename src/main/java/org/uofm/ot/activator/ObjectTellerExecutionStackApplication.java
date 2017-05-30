package org.uofm.ot.activator;

import java.util.Collections;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.stereotype.Component;

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

	@Component
	class adapterInfoList implements InfoContributor {

		@Override
		public void contribute(Info.Builder builder) {
			builder.withDetail("example", Collections.singletonMap("key", "value"));
		}
	}


}

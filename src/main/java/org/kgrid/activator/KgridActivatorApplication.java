package org.kgrid.activator;

import org.kgrid.activator.controller.ActivationController;
import org.kgrid.activator.services.AdapterLoader;
import org.kgrid.activator.services.AdapterResolver;
import org.kgrid.activator.services.Endpoint;
import org.kgrid.shelf.repository.CompoundDigitalObjectStore;
import org.kgrid.shelf.repository.CompoundDigitalObjectStoreFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.bind.annotation.CrossOrigin;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

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

    @Profile("dev")
    @Configuration
    public class DevSecurityConfiguration extends WebSecurityConfigurerAdapter {
        @Override
        public void configure(WebSecurity web)  {
            web.ignoring().antMatchers("/**");
        }
    }

    @Profile("!dev")
    @Configuration
    public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.csrf().disable()
                    .authorizeRequests()
                    .requestMatchers(EndpointRequest.to(HealthEndpoint.class)).authenticated()
                    .requestMatchers(EndpointRequest.to(InfoEndpoint.class)).authenticated()
                    .antMatchers(HttpMethod.GET, "/activate").authenticated()
                    .antMatchers(HttpMethod.POST, "/kos/manifest").authenticated()
                    .antMatchers(HttpMethod.POST, "/kos/manifest-list").authenticated()
                    .antMatchers(HttpMethod.POST, "/kos").authenticated()
                    .antMatchers(HttpMethod.DELETE, "/kos/{naan}/{name}").authenticated()
                    .antMatchers(HttpMethod.DELETE, "/kos/{naan}/{name}/{version}").authenticated()
                    .antMatchers(HttpMethod.PUT, "/kos/{naan}/{name}").authenticated()
                    .antMatchers(HttpMethod.PUT, "/kos/{naan}/{name}/{version}").authenticated()
                    .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
                    .and()
                    .httpBasic();
        }
    }
}

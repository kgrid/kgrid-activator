package org.kgrid.activator;

import java.net.URI;
import java.util.List;
import java.util.Map;
import org.kgrid.activator.domain.Endpoint;
import org.kgrid.activator.services.ActivationService;
import org.kgrid.activator.services.AdapterLoader;
import org.kgrid.activator.services.KoLoader;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.shelf.repository.CompoundDigitalObjectStore;
import org.kgrid.shelf.repository.CompoundDigitalObjectStoreFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
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

@SpringBootApplication(scanBasePackages = {"org.kgrid.shelf", "org.kgrid.activator", "org.kgrid.adapter"})
@CrossOrigin
public class KgridActivatorApplication implements CommandLineRunner {

    final
    AdapterLoader adapterLoader;

    final
    ActivationService activationService;

    final
    KoLoader koLoader;

    public KgridActivatorApplication(AdapterLoader adapterLoader, ActivationService activationService, KoLoader koLoader) {
        this.adapterLoader = adapterLoader;
        this.activationService = activationService;
        this.koLoader = koLoader;
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(KgridActivatorApplication.class)
                .build()
                .run(args);
    }

    @Primary
    @Bean
    public static CompoundDigitalObjectStore getCDOStore(
            @Value("${kgrid.shelf.cdostore.url:filesystem:file://shelf}") String cdoStoreURI) {
        cdoStoreURI = cdoStoreURI.replace(" ", "%20");
        return CompoundDigitalObjectStoreFactory.create(cdoStoreURI);
    }

    @Override
    public void run(String... strings) {

        // Load KOs and create endpoints (don't activate yet)
        final Map<URI, Endpoint> eps = koLoader.loadAllKos();
        activationService.getEndpointMap().putAll(eps);

        List<Adapter> adapters = adapterLoader.loadAdapters();
        activationService.setAdapters(adapters);
// TODO: require adapaters to initiate refresh as part of initialization
        adapterLoader.initializeAdapters(adapters);

// TODO: Remove startup activation for built-n adapters
        activationService.activateEngine("javascript");
        activationService.activateEngine("resource");

    }

    @Profile("dev")
    @Configuration
    public class DevWebSecurityConfiguration extends WebSecurityConfigurerAdapter {
        @Override
        public void configure(WebSecurity web) {
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
                    .requestMatchers(EndpointRequest.to(InfoEndpoint.class)).authenticated()
                    .mvcMatchers(HttpMethod.GET, "/activate").authenticated()
                    .mvcMatchers(HttpMethod.POST, "/kos/manifest").authenticated()
                    .mvcMatchers(HttpMethod.POST, "/kos/manifest-list").authenticated()
                    .mvcMatchers(HttpMethod.POST, "/kos").authenticated()
                    .mvcMatchers(HttpMethod.DELETE, "/kos/{naan}/{name}/{version}").authenticated()
                    .mvcMatchers(HttpMethod.PUT, "/kos/{naan}/{name}/{version}").authenticated()
                    .requestMatchers(EndpointRequest.to("activation")).authenticated()
                    .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
                    .and().cors().and()
                    .httpBasic();
        }
    }
}

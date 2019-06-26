package org.kgrid.activator;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.basePackage("org.kgrid"))
        .paths(PathSelectors.any())
        .build().apiInfo(getApiInfo());
  }
  private ApiInfo getApiInfo() {
    return new ApiInfoBuilder()
        .title("Knowledge Grid Activator API")
        .description("The Knowledge Grid Activator API provides access to Knowledge Object and Endpoint services.<ul> "
            + "<li><b>Knowledge Object API</b> provides basic read, import and export functions. "
            + "Further details, examples can be found at <a target=_blank href=\"http://kgrid.org\">Knowledge Object API</a>"
            + "<li><b>Endpoint API</b> displays the activator endpoints loaded, the endpoints are the activatoed services "
            + "defined in the service specifications of each Knowledge Object Implementation. "
            + "Further details, examples can be found at <a target=_blank href=\"http://kgrid.org\">Endpoint API</a> "
            + "<li><b>Administrative API</b> basic <a target=_blank href=\"/info\">info</a> and <a target=_blank href=\"/health\">health</a> endpoints are defined "
            + "</ul>")
        .termsOfServiceUrl("http://kgrid.org")
        .license("Apache License Version 2.0")
        .licenseUrl("https://github.com/kgrid")
        .version("2.0")
        .build();
  }
}

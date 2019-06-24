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
        .description("The Knowledge Grid Activator API provides access to Knowledge Object and Endpoint services.")
        .termsOfServiceUrl("http://kgrid.org")
        .license("Apache License Version 2.0")
        .licenseUrl("https://github.com/kgrid")
        .version("2.0")
        .build();
  }
}

package org.kgrid.activator.controller;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import org.kgrid.activator.services.Endpoint;
import org.kgrid.shelf.domain.ArkId;
import org.springframework.hateoas.ResourceSupport;

@ApiModel(value="Endpoint", description="Endpoint defines the service available via the KGrid Activator ")
public class EndpointResource extends ResourceSupport {

  private String title;
  private String endpointPath;
  private String servicePath;
  private LocalDateTime activated;


  public EndpointResource(Endpoint endpoint) {

    this.title = endpoint.getImpl().get("title").textValue();
    this.endpointPath = endpoint.getPath().replaceFirst("-","/");

    this.servicePath = new ArkId(
        endpoint.getImpl().get("identifier").textValue()).
                          getSlashArkImplementation()+"/service";

    this.activated = endpoint.getActivated();
  }

  @ApiModelProperty(value = "The Knowledge Object Implementation title")
  public String getTitle() {
    return title;
  }

  @ApiModelProperty(value = "Path to the endpoint")
  public String getEndpointPath() {
    return endpointPath;
  }

  @ApiModelProperty(value = "Path to the Knowledge Object Implementation service specification ")
  public String getServicePath() {
    return servicePath;
  }

  @ApiModelProperty(value = "The time and date the endpoint was loaded and activated in the activator")
  public LocalDateTime getActivated() {
    return activated;
  }
}

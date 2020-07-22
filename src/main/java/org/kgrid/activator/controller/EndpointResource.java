package org.kgrid.activator.controller;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import org.kgrid.activator.services.Endpoint;
import org.kgrid.shelf.domain.ArkId;
import org.springframework.hateoas.ResourceSupport;

@ApiModel(value="Endpoint", description="Endpoint defines the service available via the KGrid Activator ")
public class EndpointResource extends ResourceSupport {

  private final Endpoint endpoint;

  public EndpointResource(Endpoint endpoint) {

    this.endpoint = endpoint;
  }

  @ApiModelProperty(value = "The time and date the endpoint was loaded and activated in the activator")
  public Endpoint getEndpoint() {return endpoint;}

}

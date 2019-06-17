package org.kgrid.activator.controller;

import java.time.LocalDateTime;
import org.kgrid.activator.services.Endpoint;
import org.kgrid.shelf.domain.ArkId;
import org.springframework.hateoas.ResourceSupport;

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

  public String getTitle() {
    return title;
  }

  public String getEndpointPath() {
    return endpointPath;
  }

  public String getServicePath() {
    return servicePath;
  }

  public LocalDateTime getActivated() {
    return activated;
  }
}

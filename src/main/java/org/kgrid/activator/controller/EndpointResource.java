package org.kgrid.activator.controller;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.kgrid.activator.services.Endpoint;
import org.kgrid.shelf.domain.ArkId;
import org.springframework.hateoas.ResourceSupport;

import java.time.LocalDateTime;

@ApiModel(value = "Endpoint", description = "Endpoint defines the service available via the KGrid Activator ")
public class EndpointResource extends ResourceSupport {

    private String title;
    private String endpointPath;
    private String servicePath;
    private LocalDateTime activated;
    private String status;


    public EndpointResource(Endpoint endpoint) {

        this.title = endpoint.getMetadata().get("title").textValue();
        this.endpointPath = endpoint.getPath().replaceFirst("-", "/");

        this.servicePath = new ArkId(
                endpoint.getMetadata().get("identifier").textValue()).
                getSlashArk() + "/service";

        this.activated = endpoint.getActivated();
        this.status = endpoint.getStatus();
    }

    @ApiModelProperty(value = "The Knowledge Object Implementation title")
    public String getTitle() {
        return title;
    }

    @ApiModelProperty(value = "Unknown endpoints status")
    public String getStatus() {
        return status;
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

package org.kgrid.activator.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.kgrid.activator.services.Endpoint;
import org.kgrid.shelf.domain.KoFields;
import org.springframework.hateoas.ResourceSupport;

import java.net.URI;
import java.time.LocalDateTime;

@ApiModel(value = "Endpoint", description = "Endpoint defines the service available via the KGrid Activator ")
public class EndpointResource extends ResourceSupport {

    private String title;
    private URI endpointPath;
    private URI servicePath;
    private LocalDateTime activated;
    private String status;


    public EndpointResource(Endpoint endpoint) {
        JsonNode metadata = endpoint.getMetadata();
        String id = metadata.get("@id").asText();
        try {
            this.title = metadata.get("title").textValue();
            this.endpointPath = endpoint.getId();
            this.servicePath = URI.create(String.format("%s/%s", id, metadata.get(KoFields.SERVICE_SPEC_TERM.asStr()).asText()));
            this.activated = endpoint.getActivated();
            this.status = endpoint.getStatus();
        } catch (Exception e) {
            this.status = "Could not create endpoint resource for malformed endpoint: " + id;
        }

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
    public URI getEndpointPath() {
        return endpointPath;
    }

    @ApiModelProperty(value = "Path to the Knowledge Object Implementation service specification ")
    public URI getServicePath() {
        return servicePath;
    }

    @ApiModelProperty(value = "The time and date the endpoint was loaded and activated in the activator")
    public LocalDateTime getActivated() {
        return activated;
    }
}

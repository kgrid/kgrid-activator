package org.kgrid.activator.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.kgrid.activator.services.Endpoint;
import org.kgrid.shelf.controller.KnowledgeObjectController;
import org.kgrid.shelf.domain.KoFields;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Objects;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@ApiModel(value = "Endpoint", description = "Endpoint defines the service available via the KGrid Activator ")
public class EndpointResource {

    private String id;
    private String title;
    private String swaggerLink;
    private URI servicePath;
    private LocalDateTime activated;
    private String status;
    private String engine;
    private String context;
    private String koLink;

    public EndpointResource(Endpoint endpoint) {
        JsonNode metadata = endpoint.getMetadata();

        final String resourceId = metadata.get("@id").asText();
        try {
            this.id = endpoint.getId().toString();
            this.title = metadata.get("title").textValue();
            this.servicePath = URI.create(String.format("%s/%s", resourceId, metadata.get(KoFields.SERVICE_SPEC_TERM.asStr()).asText()));
            this.activated = endpoint.getActivated();
            this.status = endpoint.getStatus();
            this.engine = endpoint.getEngine();
            this.swaggerLink = getSwaggerLink(endpoint);
            this.context = "http://kgrid.org/koio/contexts/knowledgeobject.jsonld";
            this.koLink = getKoLink(endpoint);
        } catch (Exception e) {
            this.status = "Could not create endpoint resource for malformed endpoint: " + resourceId;
        }

    }

    private String getSwaggerLink(Endpoint endpoint) {

        return "https://editor.swagger.io?url=" +
                linkTo(KnowledgeObjectController.class)
                        .slash("kos").slash(servicePath);

    }

    private String getKoLink(Endpoint endpoint) {

        return "/kos/" + endpoint.getMetadata().get("@id").asText();

    }

    @ApiModelProperty(value = "The object's global identifier")
    @JsonProperty("@id")
    public String getId() {
        return id;
    }

    @ApiModelProperty(value = "The Knowledge Object Implementation title")
    public String getTitle() {
        return title;
    }

    @ApiModelProperty(value = "Unknown endpoints status")
    public String getStatus() {
        return status;
    }

    @ApiModelProperty(value = "Path to the swagger ui with the service spec")
    public String getSwaggerLink() {
        return swaggerLink;
    }

    @ApiModelProperty(value = "Path to the ko resource")
    public String getKoLink() {
        return koLink;
    }

    @ApiModelProperty(value = "Path to the Knowledge Object Implementation service specification ")
    public URI getServicePath() {
        return servicePath;
    }

    @ApiModelProperty(value = "The time and date the endpoint was loaded and activated in the activator")
    public LocalDateTime getActivated() {
        return activated;
    }

    @ApiModelProperty(value = "The engine that will be used to activate and run this endpoint")
    public String getEngine() {
        return engine;
    }

    @ApiModelProperty(value = "The KOIO linked data context")
    @JsonProperty("@context")
    public String getContext() {
        return context;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EndpointResource that = (EndpointResource) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(title, that.title) &&
                Objects.equals(swaggerLink, that.swaggerLink) &&
                Objects.equals(servicePath, that.servicePath) &&
                Objects.equals(activated, that.activated) &&
                Objects.equals(status, that.status) &&
                Objects.equals(engine, that.engine) &&
                Objects.equals(context, that.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, swaggerLink, servicePath, activated, status, engine, context);
    }
}

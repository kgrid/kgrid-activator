package org.kgrid.activator.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.kgrid.activator.domain.Endpoint;
import org.kgrid.shelf.controller.KnowledgeObjectController;
import org.kgrid.shelf.domain.KoFields;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@ApiModel(value = "Endpoint", description = "Endpoint defines the service available via the KGrid Activator ")
public class EndpointResource {

    private String id;
    private String title;
    private String swaggerLink;
    private URI hasServiceSpecification;
    private LocalDateTime activated;
    private String status;
    private String detail;
    private String engine;
    private List<String> context;
    private String ko;

    public EndpointResource(Endpoint endpoint, String shelfRoot) {
        JsonNode metadata = endpoint.getMetadata();

        final String resourceId = metadata.get("@id").asText();
        this.context = new ArrayList<>();
        try {
            this.id = endpoint.getId().toString();
            this.title = metadata.get("title").textValue();
            this.hasServiceSpecification = URI.create(String.format("/%s/%s/%s", shelfRoot, resourceId, metadata.get(KoFields.SERVICE_SPEC_TERM.asStr()).asText()));
            this.activated = endpoint.getActivated();
            this.status = endpoint.getStatus();
            this.detail = endpoint.getDetail();
            this.engine = endpoint.getEngine();
            this.swaggerLink = "https://editor.swagger.io?url=" +
                    linkTo(KnowledgeObjectController.class).slash(hasServiceSpecification);
            this.context.add("http://kgrid.org/koio/contexts/knowledgeobject.jsonld");
            this.context.add("http://kgrid.org/koio/contexts/implementation.jsonld");
            this.ko = String.format("/%s/%s", shelfRoot, endpoint.getMetadata().get("@id").asText());
        } catch (Exception e) {
            this.status = "Could not create endpoint resource for malformed endpoint: " + resourceId;
        }

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

    @ApiModelProperty(value = "Unknown endpoints detail")
    public String getDetail() {
        return detail;
    }

    @ApiModelProperty(value = "Path to the swagger ui with the service spec")
    public String getSwaggerLink() {
        return swaggerLink;
    }

    @ApiModelProperty(value = "Path to the ko resource")
    public String getKnowledgeObject() {
        return ko;
    }

    @ApiModelProperty(value = "Path to the Knowledge Object Implementation service specification ")
    public URI getHasServiceSpecification() {
        return hasServiceSpecification;
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
    public List<String> getContext() {
        return context;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EndpointResource resource = (EndpointResource) o;
        return Objects.equals(id, resource.id) &&
                Objects.equals(title, resource.title) &&
                Objects.equals(swaggerLink, resource.swaggerLink) &&
                Objects.equals(hasServiceSpecification, resource.hasServiceSpecification) &&
                Objects.equals(activated, resource.activated) &&
                Objects.equals(status, resource.status) &&
                Objects.equals(engine, resource.engine) &&
                Objects.equals(context, resource.context) &&
                Objects.equals(ko, resource.ko);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, swaggerLink, hasServiceSpecification, activated, status, engine, context, ko);
    }
}

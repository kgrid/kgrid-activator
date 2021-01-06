package org.kgrid.activator.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.domain.KnowledgeObjectWrapper;

import java.net.URI;
import java.time.LocalDateTime;

public class Endpoint {

    private KnowledgeObjectWrapper wrapper;
    private Executor executor;
    private LocalDateTime activated;
    private String status;
    private String endpointName;

    public Endpoint(KnowledgeObjectWrapper wrapper, String endpointName) {
        this.wrapper = wrapper;
        this.status = "GOOD";
        this.endpointName = endpointName;
        this.activated = LocalDateTime.now();
    }

    public String getEngine() {
        return getDeployment().get("engine").asText();
    }

    public KnowledgeObjectWrapper getWrapper() {
        return wrapper;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public JsonNode getService() {
        return wrapper.getService();
    }

    public JsonNode getMetadata() {
        return wrapper.getMetadata();
    }

    public JsonNode getDeployment() {
        JsonNode postDeployment = wrapper.getDeployment().get("/" + endpointName).get("post");
        if (postDeployment == null || postDeployment.isMissingNode())
            return wrapper.getDeployment().get("/" + endpointName).get("get");
        return postDeployment;
    }

    public LocalDateTime getActivated() {
        return activated;
    }

    public String getStatus() {
        return status;
    }

    public Boolean isActive() {
        return status != null && status.equals("Activated");
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArkId getArkId() {
        ArkId arkId = new ArkId(wrapper.getMetadata().at("/identifier").asText());
        if (!arkId.hasVersion()) {
            arkId = new ArkId(wrapper.getMetadata().at("/identifier").asText() + "/" + wrapper.getMetadata().at("/version").asText());
        }
        return arkId;
    }

    public String getNaan() {
        return wrapper.getMetadata().at("/@id").asText().split("/")[0];
    }

    public String getName() {
        return wrapper.getMetadata().at("/@id").asText().split("/")[1];
    }

    public String getApiVersion() {
        return wrapper.getService().at("/info/version").asText();
    }

    public URI getId() {
        return URI.create(String.format("%s/%s/%s/%s", getNaan(), getName(), getApiVersion(), endpointName));
    }

    public String getEndpointName() {
        return endpointName;
    }

}

package org.kgrid.activator.services;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.domain.KnowledgeObjectWrapper;

import java.time.LocalDateTime;
import java.util.Map;

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
        return wrapper.getDeployment().get(endpointName).get("post");
    }

    public LocalDateTime getActivated() {
        return activated;
    }

    public String getPath() {
        String apiVersion = getApiVersion();
        return wrapper.getMetadata().at("/@id").asText() + endpointName + (apiVersion != null ? "?v=" + apiVersion : "");
    }

    public String getStatus() {
        return status;
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
        return URI.create(String.format("%s/%s/%s/%s", getNaan(), getName(), getApiVersion(), endpointName.substring(1)));
    }

    public String getEndpointName() {
        return endpointName;
    }

}

package org.kgrid.activator.services;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.domain.KnowledgeObjectWrapper;

import java.time.LocalDateTime;
import java.util.Map;

public class Endpoint {

    //TODO: Further refactor

    private KnowledgeObjectWrapper wrapper;
    private Map.Entry<String, JsonNode> pathEntry;
    private String apiVersion;
    private String path;
    private JsonNode service;
    private JsonNode metadata;
    private JsonNode deployment;
    private Executor executor;
    private LocalDateTime activated;
    private String status;
    private String endpointName;

    public Endpoint(KnowledgeObjectWrapper wrapper, Map.Entry<String, JsonNode> pathEntry, String status) {
        this.wrapper = wrapper;
        this.pathEntry = pathEntry;
        JsonNode serviceSpec = wrapper.getService();
        this.apiVersion = wrapper.getService().at("/info/version").asText();
        this.service = serviceSpec;
        this.deployment = wrapper.getDeployment().get("endpoints").get(pathEntry.getKey());
        this.metadata = wrapper.getMetadata();
        this.path = metadata.at("/@id").asText() + pathEntry.getKey() + (apiVersion != null ? "?v=" + apiVersion : "");
        this.status = status.equals("") ? "GOOD" : status;
        this.endpointName = pathEntry.getKey();
        this.activated = LocalDateTime.now();
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

    public void setService(JsonNode service) {
        this.service = service;
    }

    public JsonNode getMetadata() {
        return wrapper.getMetadata();
    }

    public JsonNode getDeployment() {
        return wrapper.getDeployment().get("endpoints").get(pathEntry.getKey());
    }

    public void setDeployment(JsonNode deployment) {
        this.deployment = deployment;
    }

    public LocalDateTime getActivated() {
        return activated;
    }

    public String getPath() {
        return metadata.at("/@id").asText() + pathEntry.getKey() + (apiVersion != null ? "?v=" + apiVersion : "");
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArkId getArkId() {
        ArkId arkId = new ArkId(metadata.at("/identifier").asText());
        if (!arkId.hasVersion()) {
            arkId = new ArkId(metadata.at("/identifier").asText() + "/" + metadata.at("/version").asText());
        }
        return arkId;
    }

    public String getNaan() {
        return metadata.at("/@id").asText().split("/")[0];
    }

    public String getName() {
        return metadata.at("/@id").asText().split("/")[1];
    }

    public String getApiVersion() {
        return this.service.at("/info/version").asText();
    }

    public URI getId() {

        return URI.create(String.format("%s/%s/%s/%s", getNaan(), getName(), getApiVersion(), endpointName.substring(1)));
    }

    public String getEndpointName() {
        return endpointName;
    }

}

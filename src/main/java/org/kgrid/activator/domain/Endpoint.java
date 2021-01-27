package org.kgrid.activator.domain;

import com.fasterxml.jackson.databind.JsonNode;
import org.kgrid.activator.EndPointResult;
import org.kgrid.activator.constants.EndpointStatus;
import org.kgrid.activator.exceptions.ActivatorEndpointNotFoundException;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.domain.KnowledgeObjectWrapper;
import org.springframework.http.MediaType;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class Endpoint implements Comparable<Endpoint> {

    private KnowledgeObjectWrapper wrapper;
    private Executor executor;
    private LocalDateTime activated;
    private String status;
    private String endpointName;
    private String detail;

    public Endpoint(KnowledgeObjectWrapper wrapper, String endpointName) {
        this.wrapper = wrapper;
        this.status = EndpointStatus.LOADED.name();
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

    public String getDetail() {
         return detail;
    }

    public Boolean isActive() {
        return status != null && status.equals(EndpointStatus.ACTIVATED.name());
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDetail(String detail) {
        this.detail = detail;
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

    public ArrayList<String> getSupportedContentTypes() {
        ArrayList<String> supportedTypes = new ArrayList<>();
        this.getService().at("/paths").get("/" + this.getEndpointName())
                .get("post").get("requestBody").get("content").fieldNames().forEachRemaining(key -> {
            supportedTypes.add(key);
        });
        return supportedTypes;
    }

    public boolean isSupportedContentType(MediaType contentType) {
        if (null == contentType) {
            return false;
        }
        final JsonNode contentTypes = this.getService()
                .at(String.format("/paths/~1%s/post/requestBody/content", endpointName));
        AtomicBoolean matches = new AtomicBoolean(false);
        contentTypes.fieldNames().forEachRemaining(key -> {
            if (contentType.toString().equals(key)) {
                matches.set(true);
            }
        });
        return matches.get();
    }

    public EndPointResult execute(Object inputs, MediaType contentType) {

        if (null == executor) {
            throw new ActivatorEndpointNotFoundException("No executor found for " + this.getId());
        }

        String contentTypeString = (null == contentType) ? "" : contentType.toString();

        final EndPointResult endPointResult = new EndPointResult(this.executor.execute(inputs, contentTypeString));
        endPointResult.getInfo().put("inputs", inputs);
        endPointResult.getInfo().put("ko", wrapper.getMetadata());
        return endPointResult;
    }

    @Override
    public int compareTo(Endpoint endpoint) {
        return endpoint.getId().compareTo(this.getId());
    }
}

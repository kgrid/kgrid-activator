package org.uofm.ot.activator.adapter.gateway;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Class for mapping the components of the /api/kernels REST responses from a Jupyter Kernel Gateway
 * Created by grosscol on 2017-06-07.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class KernelMetadata {
    @JsonProperty
    private
    String name;
    @JsonProperty
    private
    String id;
    @JsonProperty
    private
    Integer connections;
    @JsonProperty("execution_state")
    private
    String execState;
    @JsonProperty("last_activity")
    private
    String lastActivity;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getConnections() {
        return connections;
    }

    public void setConnections(Integer connections) {
        this.connections = connections;
    }

    public String getExecState() {
        return execState;
    }

    public void setExecState(String execState) {
        this.execState = execState;
    }

    public String getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(String lastActivity) {
        this.lastActivity = lastActivity;
    }

}

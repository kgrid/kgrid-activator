package org.kgrid.activator.services;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.kgrid.adapter.api.Executor;

public class Endpoint {

  private String path;
  private JsonNode service;
  private JsonNode impl;
  private JsonNode deployment;
  private Executor executor;
  private LocalDateTime activated;

  public Executor getExecutor() {
    return executor;
  }

  public void setExecutor(Executor executor) {
    this.executor = executor;
  }

  public JsonNode getService() {
    return service;
  }

  public void setService(JsonNode service) {
    this.service = service;
  }

  public JsonNode getImpl() {
    return impl;
  }

  public void setImpl(JsonNode impl) {
    this.impl = impl;
  }

  public JsonNode getDeployment() {
    return deployment;
  }

  public void setDeployment(JsonNode deployment) {
    this.deployment = deployment;
  }

  public void setActivated(LocalDateTime activated) { this.activated = activated; }

  public LocalDateTime getActivated(){ return activated; }

  public String getPath() {return path; }
  public void setPath(String path){ this.path=path;}

  public  static final class Builder {

    private JsonNode service;
    private JsonNode impl;
    private JsonNode deployment;
    private byte[] artifact;
    private String entry;
    private boolean canActivate;
    private Executor executor;

    private Builder() {
    }

    public static Builder anEndpoint() {
      return new Builder();
    }

    public Builder withService(JsonNode service) {
      this.service = service;
      return this;
    }

    public Builder withImpl(JsonNode impl) {
      this.impl = impl;
      return this;
    }

    public Builder withDeployment(JsonNode deployment) {
      this.deployment = deployment;
      return this;
    }


    public Builder withExecutor(Executor executor) {
      this.executor = executor;
      return this;
    }


    public Endpoint build() {
      Endpoint endpoint = new Endpoint();
      endpoint.setService(service);
      endpoint.setImpl(impl);
      endpoint.setDeployment(deployment);
      endpoint.setExecutor(executor);
      endpoint.activated=LocalDateTime.now();
      return endpoint;
    }


  }
}

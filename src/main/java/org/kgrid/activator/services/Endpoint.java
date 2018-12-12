package org.kgrid.activator.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.kgrid.adapter.api.Executor;

public class Endpoint {

  private JsonNode service;
  private JsonNode impl;
  private JsonNode deployment;
  private byte[] artifact;
  private String entry;
  private boolean canActivate;
  private Executor executor;

  public Executor getExecutor() {
    return executor;
  }

  public void setExecutor(Executor executor) {
    this.executor = executor;
  }

  public boolean isCanActivate() {
    return canActivate;
  }

  public void setCanActivate(boolean canActivate) {
    this.canActivate = canActivate;
  }

  public byte[] getArtifact() {
    return artifact;
  }

  public void setArtifact(byte[] artifact) {
    this.artifact = artifact;
  }

  public String getEntry() {
    return entry;
  }

  public void setEntry(String entry) {
    this.entry = entry;
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

  public static final class Builder {

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

    public Builder withArtifact(byte[] artifact) {
      this.artifact = artifact;
      return this;
    }

    public Builder withEntry(String entry) {
      this.entry = entry;
      return this;
    }

    public Builder withCanActivate(boolean canActivate) {
      this.canActivate = canActivate;
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
      endpoint.setArtifact(artifact);
      endpoint.setEntry(entry);
      endpoint.setCanActivate(canActivate);
      endpoint.setExecutor(executor);
      return endpoint;
    }
  }
}

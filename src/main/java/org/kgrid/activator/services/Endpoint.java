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
}

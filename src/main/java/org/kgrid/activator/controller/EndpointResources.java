package org.kgrid.activator.controller;

import java.util.ArrayList;
import java.util.Collection;
import org.springframework.hateoas.ResourceSupport;

/**
 * Defines a collection of EndpointResource
 */
public class EndpointResources extends ResourceSupport {

  Collection<EndpointResource> endpointResources = new ArrayList<>();


  public void addEndpointResource(EndpointResource EndpointResource){
    endpointResources.add( EndpointResource );
  }
  public int getNumberOfEndpoints(){
    return endpointResources.size();
  }
  public Collection<EndpointResource> getEndpointResources(){
    return endpointResources;
  }



}

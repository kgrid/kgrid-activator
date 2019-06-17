package org.kgrid.activator.controller;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.kgrid.activator.services.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/endpoint" )
public class EndpointController {

  @Autowired
  private Map<String, Endpoint> endpoints;

  @GetMapping(produces = { "application/hal+json" })
  public Resources<EndpointResource> findAll() {

    Collection<EndpointResource> resources = new ArrayList();

    endpoints.forEach((s, endpoint) -> {


      EndpointResource resource = new EndpointResource(endpoint);

      Link self = linkTo(ActivationController.class).
          slash(resource.getEndpointPath()).withSelfRel();

      Link swaggerEditor = new Link("https://editor.swagger.io?url="+
          linkTo(ActivationController.class).slash(resource.getServicePath()),
          "swagger_editor");


      resource.add(self);
      resource.add(swaggerEditor);

      resources.add(resource);


    });

    Link link = linkTo(EndpointController.class).withSelfRel();
    Resources<EndpointResource> result = new Resources<>(resources, link);
    return result;
  }

}

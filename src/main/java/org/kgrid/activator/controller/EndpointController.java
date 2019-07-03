package org.kgrid.activator.controller;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.kgrid.activator.services.Endpoint;
import org.kgrid.shelf.controller.KnowledgeObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class EndpointController {

  @Autowired
  private Map<String, Endpoint> endpoints;
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @GetMapping(value= "/endpoints", produces = MediaType.APPLICATION_JSON_VALUE)
  public  Resources<EndpointResource>  findAllEndpoints() {


    log.info("find all endpoints");
    Collection<EndpointResource> resources = new ArrayList();

    endpoints.forEach((s, endpoint) -> {

      EndpointResource resource = createEndpointResource(endpoint);

      resources.add(resource);

    });

    Link link = linkTo(EndpointController.class).withSelfRel();
    Resources<EndpointResource> result = new Resources<>(resources, link);
    return result;
  }


  @GetMapping( value = "/endpoints/{naan}/{name}/{implementation}/{endpoint}", produces = MediaType.APPLICATION_JSON_VALUE)
  public EndpointResource findEndpoint(
      @PathVariable String naan,
      @PathVariable String name,
      @PathVariable String implementation,
      @PathVariable String endpoint) {

    log.info("getting ko endpoint " + naan + "/" + name + "/" + implementation);

    final String key = naan + "-" + name + "/" + implementation + "/" + endpoint;

    EndpointResource resource = createEndpointResource( endpoints.get( key ));

    return resource;
  }

  private EndpointResource createEndpointResource(Endpoint endpoint) {
    EndpointResource resource = new EndpointResource(endpoint);
    Link self = linkTo(EndpointController.class).
        slash(resource.getEndpointPath()).withSelfRel();

    Link swaggerEditor = new Link("https://editor.swagger.io?url="+
        linkTo(KnowledgeObjects.class).slash(resource.getServicePath()),
        "swagger_editor");

    resource.add(self);
    resource.add(swaggerEditor);

    return resource;
  }
}

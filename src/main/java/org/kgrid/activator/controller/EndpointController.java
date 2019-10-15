package org.kgrid.activator.controller;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import java.util.Map;
import java.util.Map.Entry;
import org.kgrid.activator.ActivatorException;
import org.kgrid.activator.services.Endpoint;
import org.kgrid.activator.services.EndpointId;
import org.kgrid.shelf.controller.KnowledgeObjectContoller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class EndpointController {

  @Autowired
  private Map<EndpointId, Endpoint> endpoints;
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @GetMapping(value= "/endpoints", produces = MediaType.APPLICATION_JSON_VALUE)
  public EndpointResources  findAllEndpoints() {


    log.info("find all endpoints");
    EndpointResources resources = new EndpointResources();

    endpoints.forEach((s, endpoint) -> {
        EndpointResource resource = createEndpointResource(endpoint);

        resources.addEndpointResource(resource);
    });

    resources.add(linkTo(EndpointController.class).withSelfRel());
    return resources;
  }

  @GetMapping( value = "/endpoints/{naan}/{name}/{endpointName}", produces = MediaType.APPLICATION_JSON_VALUE)
  public EndpointResource findEndpoint(
      @PathVariable String naan,
      @PathVariable String name,
      @PathVariable String endpointName,
      @RequestParam(name = "v", required = false) String version) {

    log.info("getting ko endpoint " + naan + "/" + name );

    EndpointId id = new EndpointId(naan, name, version, endpointName);

    Endpoint endpoint = null;
    if(version == null){
      for(Entry<EndpointId, Endpoint> entry : endpoints.entrySet() ){
        if(entry.getKey().getArkId().getSlashArk().equals(id.getArkId().getSlashArk())
            && entry.getKey().getEndpointName().equals("/" + endpointName)) {
          endpoint = entry.getValue();
          break;
        }
      }
    } else {
      endpoint = endpoints.get(id);
    }

    if(endpoint == null) {
      throw new ActivatorException("Cannot find endpoint with id " + id);
    }

    EndpointResource resource = createEndpointResource(endpoint);

    return resource;
  }

  private EndpointResource createEndpointResource(Endpoint endpoint) {
    EndpointResource resource = new EndpointResource(endpoint);
    Link self = linkTo(EndpointController.class).slash("endpoints").
        slash(resource.getEndpointPath()).withSelfRel();

    Link swaggerEditor = new Link("https://editor.swagger.io?url="+
        linkTo(KnowledgeObjectContoller.class).slash("kos").slash(resource.getServicePath()),
        "swagger_editor");

    resource.add(self);
    resource.add(swaggerEditor);

    return resource;
  }
}

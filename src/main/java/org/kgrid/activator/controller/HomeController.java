package org.kgrid.activator.controller;


import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import org.kgrid.shelf.controller.KnowledgeObjectContoller;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

  @GetMapping( value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
  public ActivatorResource home() {

    ActivatorResource activatorResource = new ActivatorResource();

    Link  health = linkTo(HomeController.class).slash("health").withRel("activator_health");
    activatorResource.add(health);
    Link  info = linkTo(HomeController.class).slash("info").withRel("activator_info");
    activatorResource.add(info);
    Link  apiDocs = new Link("https://kgrid.org/api").withRel("activator_api_docs");
    activatorResource.add(apiDocs);

    Link  koList = linkTo(KnowledgeObjectContoller.class).slash("kos").withRel("activator_ko_list");
    activatorResource.add(koList);

    Link  endpointList = linkTo(HomeController.class).slash("endpoints").withRel("activator_endpoint_list");
    activatorResource.add(endpointList);

    return activatorResource;
  }

  private class ActivatorResource extends ResourceSupport {

    private String description = "KGrid Activator API Starting Point!!";

    public String getDescription() {
      return description;
    }
  }

}

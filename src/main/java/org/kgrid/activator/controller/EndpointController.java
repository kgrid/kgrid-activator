package org.kgrid.activator.controller;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.kgrid.activator.services.Endpoint;
import org.kgrid.shelf.controller.ShelfController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/endpoints" )
@Api(tags = "Endpoint API" )
public class EndpointController {

  @Autowired
  private Map<String, Endpoint> endpoints;


  @GetMapping(produces = { "application/hal+json" })
  @ApiOperation(value = "Finds all knowledge object endpoints",
      notes = "Returns a collection of endpoints",
      response = EndpointResource.class,
      responseContainer = "List")
  public Resources<EndpointResource> findAllEndpoints() {

    Collection<EndpointResource> resources = new ArrayList();


    endpoints.forEach((s, endpoint) -> {

      EndpointResource resource = createEndpointResource(endpoint);

      resources.add(resource);

    });

    Link link = linkTo(EndpointController.class).withSelfRel();
    Resources<EndpointResource> result = new Resources<>(resources, link);
    result.add(linkTo(EndpointController.class).withSelfRel());
    return result;
  }


  @GetMapping( value = {"/{naan}/{name}/{version}/{endpoint}"}, produces = { "application/hal+json" })
  @ResponseStatus(HttpStatus.OK)
  @ApiOperation(value = "Finds a knowledge object's endpoint based ",
      notes = "Returns the Endpoint Resource which has access to the knowledge object implementation, "
          + "the service description in the form of an Open Api specification, etc")
  public EndpointResource findEndpoint(
      @ApiParam(value="Name Assigning Authority unique number", example="hello")
      @PathVariable String naan,
      @ApiParam(value="Ark Name", example="world") @PathVariable String name,
      @ApiParam(value="Ark Implementation", example="v0.1.0")  @PathVariable String version,
      @ApiParam(value="Endpoint Path", example="welcome") @PathVariable String endpoint) {

    final String key = naan + "-" + name + "/" + version + "/" + endpoint;

    EndpointResource resource = createEndpointResource( endpoints.get( key ));

    return resource;
  }

  private EndpointResource createEndpointResource(Endpoint endpoint) {
    EndpointResource resource = new EndpointResource(endpoint);
    Link self = linkTo(EndpointController.class).
        slash(resource.getEndpointPath()).withSelfRel();

    Link swaggerEditor = new Link("https://editor.swagger.io?url="+
        linkTo(ShelfController.class).slash(resource.getServicePath()),
        "swagger_editor");

    resource.add(self);
    resource.add(swaggerEditor);

    return resource;
  }
}

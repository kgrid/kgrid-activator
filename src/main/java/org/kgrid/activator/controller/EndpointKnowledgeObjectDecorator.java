package org.kgrid.activator.controller;

import org.kgrid.activator.EndPoint;
import org.kgrid.activator.services.ActivationService;
import org.kgrid.activator.services.ServiceDescriptionService;
import org.kgrid.shelf.controller.KnowledgeObjectDecorator;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.domain.KnowledgeObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;

@Service
@Primary
class EndpointKnowledgeObjectDecorator implements KnowledgeObjectDecorator {

  @Autowired
  private ActivationService activationService;

  @Autowired
  private ServiceDescriptionService serviceDescriptionService;

  @Override
  public void decorate(KnowledgeObject knowledgeObject, RequestEntity requestEntity) {

    if (activationService.getEndpoints().containsKey(activationService.getKnowleledgeObjectPath(
        knowledgeObject)+ activationService.getEndPointPath(knowledgeObject))) {

      knowledgeObject.getMetadata().put("endpoint",
          requestEntity.getUrl() + "/" + activationService.getEndPointPath(
              knowledgeObject));
    }
  }
}
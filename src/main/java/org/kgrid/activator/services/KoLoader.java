package org.kgrid.activator.services;

import org.kgrid.activator.domain.Endpoint;
import org.kgrid.activator.exceptions.ActivatorEndpointNotFoundException;
import org.kgrid.activator.exceptions.ActivatorException;
import org.kgrid.shelf.ShelfResourceNotFound;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.domain.KnowledgeObjectWrapper;
import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KoLoader {
    Logger log = LoggerFactory.getLogger(KoLoader.class);

    private final KnowledgeObjectRepository knowledgeObjectRepository;
    private final KoValidationService validationService;

    public KoLoader(KnowledgeObjectRepository knowledgeObjectRepository,
                    KoValidationService validationService) {
        this.knowledgeObjectRepository = knowledgeObjectRepository;
        this.validationService = validationService;
    }

    public Map<URI, Endpoint> loadOneKo(ArkId arkId) {
        Map<URI, Endpoint> endpoints = new HashMap<>();
        try {
            KnowledgeObjectWrapper wrapper = knowledgeObjectRepository.getKow(arkId);
            validationService.validateKow(wrapper);
            wrapper.getDeployment().fields().forEachRemaining(endpointName -> {
                Endpoint endpoint = getEndpoint(wrapper, arkId, endpointName.getKey());
                endpoints.put(endpoint.getId(), endpoint);
            });
        } catch (ActivatorException e) {
            throw new ActivatorException(String.format("Cannot load ko %s, cause: %s", arkId.getFullArk(), e.getMessage()), e);
        } catch (ShelfResourceNotFound e) {
            throw new ActivatorEndpointNotFoundException(String.format("Cannot load ko %s, cause: %s", arkId.getFullArk(), e.getMessage()), e);
        }
        return endpoints;
    }

    public Map<URI, Endpoint> loadSomeKos(List<ArkId> arkIds) {
        return arkIds.stream()
                .flatMap(arkId -> {
                    try {
                        return loadOneKo(arkId).entrySet().stream();
                    } catch (ActivatorException e) {
                        log.warn(e.getMessage());
                        return null;
                    }
                }).filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<URI, Endpoint> loadAllKos() {
        List<ArkId> allArkIds = new ArrayList<>(knowledgeObjectRepository.findAll().keySet());
        return loadSomeKos(allArkIds);
    }

    private Endpoint getEndpoint(KnowledgeObjectWrapper wrapper, ArkId arkId, String endpointName) {
        Endpoint endpoint = new Endpoint(wrapper, endpointName.substring(1), knowledgeObjectRepository.getObjectLocation(arkId));
        validationService.validateEndpoint(endpoint);
        return endpoint;
    }
}

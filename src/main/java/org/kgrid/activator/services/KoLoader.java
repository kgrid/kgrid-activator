package org.kgrid.activator.services;

import org.kgrid.activator.domain.Endpoint;
import org.kgrid.activator.exceptions.ActivatorException;
import org.kgrid.shelf.ShelfResourceNotFound;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.domain.KnowledgeObjectWrapper;
import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KoLoader {

    final Logger log = LoggerFactory.getLogger(KoLoader.class);

    private final KnowledgeObjectRepository knowledgeObjectRepository;
    private final Map<URI, Endpoint> endpointMap;
    private final KoValidationService validationService;

    public KoLoader(KnowledgeObjectRepository knowledgeObjectRepository, Map<URI, Endpoint> endpointMap, KoValidationService validationService) {
        this.knowledgeObjectRepository = knowledgeObjectRepository;
        this.endpointMap = endpointMap;
        this.validationService = validationService;
    }

    public Map<URI, Endpoint> loadOneKo(ArkId arkId) {
        Map<URI, Endpoint> endpoints = new HashMap<>();
        try {
            KnowledgeObjectWrapper wrapper = knowledgeObjectRepository.getKow(arkId);
            validationService.validateKow(wrapper);
            wrapper.getDeployment().fields().forEachRemaining(endpointName -> {
                Endpoint endpoint = getEndpoint(wrapper, endpointName.getKey());
                endpoints.put(endpoint.getId(), endpoint);
            });
        } catch (ActivatorException | ShelfResourceNotFound e) {
            throw new ActivatorException(String.format("Cannot load ko %s, %s", arkId.getFullArk(), e.getMessage()), e);
        }
        endpointMap.putAll(endpoints);
        return endpoints;
    }

    public Map<URI, Endpoint> loadAllKos() {
        return knowledgeObjectRepository.findAll().keySet().stream()
                .flatMap(arkId -> loadOneKo(arkId).entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<URI, Endpoint> loadSomeKos(List<ArkId> arkIds) {
        return arkIds.stream()
                .flatMap(arkId -> loadOneKo(arkId).entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Endpoint getEndpoint(KnowledgeObjectWrapper wrapper, String endpointName) {
        Endpoint endpoint = new Endpoint(wrapper, endpointName.substring(1));
        validationService.validateEndpoint(endpoint);
        if (endpointMap.containsKey(endpoint.getId())) {
            log.warn(String.format("Overwriting duplicate endpoint: %s", endpoint.getId()));
        }
        return endpoint;
    }
}

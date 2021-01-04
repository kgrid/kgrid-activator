package org.kgrid.activator;

import com.fasterxml.jackson.databind.JsonNode;
import org.kgrid.activator.services.Endpoint;
import org.kgrid.activator.services.KoValidationService;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.domain.KnowledgeObjectWrapper;
import org.kgrid.shelf.repository.KnowledgeObjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@Service
public class EndpointLoader {

    final Logger log = LoggerFactory.getLogger(EndpointLoader.class);

    @Autowired
    KnowledgeObjectRepository knowledgeObjectRepository;

    @Autowired
    KoValidationService koValidationService;

    /**
     * Creates endpoints based on the Implementations specification. If endpoint resources can not be
     * found no endpoint will be created.
     *
     * @return collection of endpoints
     */
    public Map<URI, Endpoint> load(ArkId ark) {

        Map<URI, Endpoint> endpoints = new HashMap<>();

        if (ark.hasVersion()) {

            log.info("ArkId: " + ark.getFullArk());

            // load required activation files for KO with `ark`
            // and create a new Endpoint and put into `endpoints` map under `/naan/name/version/endpoint`
            loadKoImplementation(ark, endpoints);

        } else {
            JsonNode knowledgeObjectMetadata = knowledgeObjectRepository.findKnowledgeObjectMetadata(ark);
            if (knowledgeObjectMetadata.isArray()) {
                knowledgeObjectMetadata.forEach(
                        ko -> {
                            if (ko.has("version")) {
                                ArkId id = new ArkId(ark.getNaan(), ark.getName(), (ko.get("version").asText()));
                                loadKoImplementation(id, endpoints);
                            }
                        });
            }
        }
        return endpoints;
    }

    private void loadKoImplementation(ArkId ark, Map<URI, Endpoint> endpoints) {
        log.info("Load KO Implementation {}", ark.getFullArk());

        try {
            KnowledgeObjectWrapper wrapper = knowledgeObjectRepository.getKow(ark);
            koValidationService.validateMetadata(wrapper.getMetadata());
            JsonNode serviceSpec = wrapper.getService();
            koValidationService.validateServiceSpecification(serviceSpec);

            serviceSpec
                    .get("paths")
                    .fields()
                    .forEachRemaining(
                            path -> {
                                Endpoint endpoint = new Endpoint(wrapper, path.getKey().substring(1));

                                try {
                                    koValidationService.validateEndpoint(endpoint);
                                } catch (ActivatorException e) {
                                    endpoint.setStatus(e.getMessage());
                                }
                                endpoints.put(endpoint.getId(), endpoint);
                            });

        } catch (Exception e) {
            final ActivatorException activatorException =
                    new ActivatorException("Failed to load " + ark.getSlashArkVersion(), e);
            log.warn(activatorException.getMessage());
        }
    }

    /**
     * Loads all the endpoints
     *
     * @return collection of endpoints
     */
    public Map<URI, Endpoint> load() {
        Map<ArkId, JsonNode> kos = knowledgeObjectRepository.findAll();
        Map<URI, Endpoint> endpoints = new HashMap<>();

        for (Entry<ArkId, JsonNode> ko : kos.entrySet()) {
            Map<URI, Endpoint> endpointsToAdd = load(ko.getKey());
            checkForDuplicates(endpoints, endpointsToAdd);
            endpoints.putAll(endpointsToAdd);
        }

        return endpoints;
    }

    private void checkForDuplicates(Map<URI, Endpoint> endpoints, Map<URI, Endpoint> endpointsToAdd) {
            for (Map.Entry<URI, Endpoint> entry : endpointsToAdd.entrySet()) {
                if (endpoints.containsKey(entry.getKey())) {
                    log.warn(String.format("Overwriting duplicate endpoint: %s", entry.getKey()));
                }
            }
    }

}

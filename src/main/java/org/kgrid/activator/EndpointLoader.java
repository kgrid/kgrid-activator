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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

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
            loadKOImplentation(ark, endpoints);

        } else {
            JsonNode knowledgeObjectMetadata = knowledgeObjectRepository.findKnowledgeObjectMetadata(ark);
            if (knowledgeObjectMetadata.isArray()) {
                knowledgeObjectMetadata.forEach(
                        ko -> {
                            if (ko.has("version")) {
                                ArkId id = new ArkId(ark.getNaan(), ark.getName(), (ko.get("version").asText()));
                                loadKOImplentation(id, endpoints);
                            }
                        });
            }
        }
        return endpoints;
    }

    private void loadKOImplentation(ArkId ark, Map<URI, Endpoint> endpoints) {
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
                                String status = "";
                                try {
                                    koValidationService.validateActivatability(path.getKey(),
                                            serviceSpec, wrapper.getDeployment());
                                } catch (ActivatorException e) {
                                    status = e.getMessage();
                                }

                                Endpoint endpoint = new Endpoint(wrapper, path, status);

                                endpoints.put(endpoint.getId(), endpoint);
                            });

        } catch (Exception e) {
            final ActivatorException activatorException =
                    new ActivatorException("Failed to load " + ark.getSlashArkVersion(), e);
            log.warn(activatorException.getMessage());
        }
    }

    //TODO: Remove the usage of `x-kgrid-activation`
    private JsonNode getEndpointDeployment(JsonNode deploymentSpecification, Entry<String, JsonNode> path) {
        JsonNode spec = path.getValue().get("post").get("x-kgrid-activation");

        if (spec == null && deploymentSpecification != null) {
            spec = deploymentSpecification.get("endpoints").get(path.getKey());

        } else {
            log.warn(
                    "Extension of `x-kgrid-activation` has been deprecated from the service specification. Please use the deployment specification file instead.");
        }
        return spec;
    }

    /**
     * Loads all the endpoints
     *
     * @return collection of endpoints
     */
    public TreeMap<URI, Endpoint> load() {
        Map<ArkId, JsonNode> kos = knowledgeObjectRepository.findAll();
        Map<URI, Endpoint> temp = new HashMap<>();

        for (Entry<ArkId, JsonNode> ko : kos.entrySet()) {
            temp.putAll(load(ko.getKey()));
        }

        // Putting everything in a treemap sorts them alphabetically
        TreeMap<URI, Endpoint> endpoints = new TreeMap<>(Collections.reverseOrder());
        endpoints.putAll(temp);

        return endpoints;
    }

    URI getKORepoLocation() {
        return knowledgeObjectRepository.getKoRepoLocation();
    }
}

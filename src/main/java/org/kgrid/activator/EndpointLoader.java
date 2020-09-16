package org.kgrid.activator;

import com.fasterxml.jackson.databind.JsonNode;
import org.kgrid.activator.services.Endpoint;
import org.kgrid.activator.services.EndpointId;
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
    public Map<EndpointId, Endpoint> load(ArkId ark) {

        Map<EndpointId, Endpoint> endpoints = new HashMap<>();

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

    private void loadKOImplentation(ArkId ark, Map<EndpointId, Endpoint> endpoints) {
        log.info("Load KO Implementation {}", ark.getFullArk());

        try {
            KnowledgeObjectWrapper wrapper = knowledgeObjectRepository.getKow(ark);
            JsonNode deploymentSpec = wrapper.getDeployment();
            JsonNode metadata = wrapper.getMetadata();
            koValidationService.validateMetadata(metadata);
            JsonNode serviceSpec = wrapper.getService();
            koValidationService.validateServiceSpecification(serviceSpec);

            String apiVersion = serviceSpec.at("/info/version").asText();

            serviceSpec
                    .get("paths")
                    .fields()
                    .forEachRemaining(
                            path -> {
                                String status = "";
                                try {
                                    koValidationService.validateActivatability(path.getKey(),
                                            serviceSpec, deploymentSpec);
                                } catch (ActivatorException e) {
                                    status = e.getMessage();
                                }

                                Endpoint endpoint =
                                        Endpoint.Builder.anEndpoint()
                                                .withService(serviceSpec)
                                                .withDeployment(deploymentSpec.get("endpoints").get(path.getKey()))
                                                .withMetadata(metadata)
                                                .withStatus(status.equals("") ? "GOOD" : status)
                                                .withPath(
                                                        metadata.at("/@id")
                                                                + path.getKey()
                                                                + (apiVersion != null ? "?v=" + apiVersion : ""))
                                                .build();
                                endpoints.put(new EndpointId(endpoint.getNaan(), endpoint.getName(), endpoint.getApiVersion(), path.getKey()), endpoint);
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
    public Map<EndpointId, Endpoint> load() {
        Map<ArkId, JsonNode> kos = knowledgeObjectRepository.findAll();
        Map<EndpointId, Endpoint> temp = new HashMap<>();

        for (Entry<ArkId, JsonNode> ko : kos.entrySet()) {
            temp.putAll(load(ko.getKey()));
        }

        // Putting everything in a treemap sorts them alphabetically
        TreeMap<EndpointId, Endpoint> endpoints = new TreeMap<>(Collections.reverseOrder());
        endpoints.putAll(temp);

        return endpoints;
    }

    URI getKORepoLocation() {
        return knowledgeObjectRepository.getKoRepoLocation();
    }
}

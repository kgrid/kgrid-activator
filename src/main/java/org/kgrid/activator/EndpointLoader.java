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
            loadKOImplemtation(ark, endpoints);

        } else {
            JsonNode knowledgeObjectMetadata = knowledgeObjectRepository.findKnowledgeObjectMetadata(ark);
            if (knowledgeObjectMetadata.isArray()) {
                knowledgeObjectMetadata.forEach(
                        ko -> {
                            if (ko.has("version")) {
                                ArkId id = new ArkId(ark.getNaan(), ark.getName(), (ko.get("version").asText()));
                                loadKOImplemtation(id, endpoints);
                            }
                        });
            }
        }
        return endpoints;
    }


    private void loadKOImplemtation(ArkId ark, Map<EndpointId, Endpoint> endpoints) {
        log.info("Load KO Implementation {}", ark.getFullArk());


        try {

            KnowledgeObjectWrapper wrapper = knowledgeObjectRepository.getKow(ark);
            koValidationService.validateMetadata(wrapper.getMetadata());
            koValidationService.validateServiceSpecification(wrapper.getService());

            String apiVersion = wrapper.getService().at("/info/version").asText();

            wrapper.getService()
                    .get("paths")
                    .fields()
                    .forEachRemaining(
                            path -> {
                                String status = "";
                                try {
                                    koValidationService.validateActivatability(path.getKey(),
                                            wrapper.getService(), wrapper.getDeployment());
                                } catch (ActivatorException e) {
                                    status = e.getMessage();
                                }

                                JsonNode endpointDeployment = getEndpointDeployment(wrapper.getDeployment(), path);
                                ArkId endpointArk = new ArkId(ark.getNaan(), ark.getName(), apiVersion);
                                Endpoint endpoint =
                                        buildEndpoint(ark, apiVersion, wrapper.getMetadata(), wrapper.getService(), path, status, endpointDeployment);
                                endpoints.put(new EndpointId(endpointArk, path.getKey()), endpoint);
                            });

        } catch (Exception e) {
            final ActivatorException activatorException =
                    new ActivatorException("Failed to load " + ark.getSlashArkVersion(), e);
            log.warn(activatorException.getMessage());
        }
    }

    private Endpoint buildEndpoint(
            ArkId ark,
            String apiVersion,
            JsonNode koMetadata,
            JsonNode serviceSpecification,
            Entry<String, JsonNode> path,
            String status,
            JsonNode spec) {
        return Endpoint.Builder.anEndpoint()
                .withService(serviceSpecification)
                .withDeployment(spec)
                .withMetadata(koMetadata)
                .withStatus(status.equals("") ? "GOOD" : status)
                .withPath(
                        ark.getSlashArk()
                                + path.getKey()
                                + (apiVersion != null ? "?v=" + apiVersion : ""))
                .build();
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

    private JsonNode getDeploymentSpec(ArkId ark, JsonNode koMetadata) {
        JsonNode deploymentSpecification;
        JsonNode t = null;
        try {
            t = knowledgeObjectRepository.findDeploymentSpecification(ark, koMetadata);
        } catch (Exception e) {
            log.warn("no deployment spec found for " + ark.getSlashArkVersion());
        }
        deploymentSpecification = t;
        return deploymentSpecification;
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

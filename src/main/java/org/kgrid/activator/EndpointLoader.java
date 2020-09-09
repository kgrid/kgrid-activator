package org.kgrid.activator;

import com.fasterxml.jackson.databind.JsonNode;
import org.kgrid.activator.services.Endpoint;
import org.kgrid.activator.services.EndpointId;
import org.kgrid.activator.services.KoValidationService;
import org.kgrid.shelf.domain.ArkId;
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
        final JsonNode koMetadata;
        final JsonNode serviceSpecification;
        final JsonNode deploymentSpecification;

        try {

            koMetadata = knowledgeObjectRepository.findKnowledgeObjectMetadata(ark);
            koValidationService.validateMetadata(koMetadata);

            serviceSpecification = knowledgeObjectRepository.findServiceSpecification(ark, koMetadata);
            koValidationService.validateServiceSpecification(serviceSpecification);

            deploymentSpecification = getDeploymentSpec(ark, koMetadata);

            serviceSpecification
                    .get("paths")
                    .fields()
                    .forEachRemaining(
                            path -> {
                                String status = "";
                                try {
                                    koValidationService.validateActivatability(path.getKey(),
                                            serviceSpecification, deploymentSpecification);
                                } catch (ActivatorException e) {
                                    status = e.getMessage();
                                }

                                JsonNode endpointDeployment = getEndpointDeployment(deploymentSpecification, path);
                                Endpoint endpoint =
                                        buildEndpoint(ark, koMetadata, serviceSpecification, path, status, endpointDeployment);
                                endpoints.put(new EndpointId(ark, path.getKey()), endpoint);
                            });
            System.out.println(endpoints);

        } catch (Exception e) {
            final ActivatorException activatorException =
                    new ActivatorException("Failed to load " + ark.getSlashArkVersion(), e);
            log.warn(activatorException.getMessage());
        }
    }

    private Endpoint buildEndpoint(
            ArkId ark,
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
                                + (ark.getVersion() != null ? "?v=" + ark.getVersion() : ""))
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

package org.kgrid.activator.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.kgrid.activator.EndpointLoader;
import org.kgrid.activator.services.ActivationService;
import org.kgrid.shelf.domain.ArkId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
@Primary
@RequestMapping({"/activate", "/refresh"})
public class ActivationController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ActivationService activationService;

    @Autowired
    private EndpointLoader endpointLoader;

    @Autowired
    private Map<URI, org.kgrid.activator.services.Endpoint> endpoints;

    /**
     * Remove all endpoints and load and activate
     *
     * @return set of activated endpoint paths
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public String activate() {
        log.info("Load and Activate all endpoints ");
        endpoints.clear();
        endpoints.putAll(endpointLoader.load());
        activationService.activate(endpoints);

        JsonArray activatedEndpoints = getActivationResults();
        return activatedEndpoints.toString();
    }

    /**
     * For KOs of a specific engine: remove endpoints, load endpoints, and activate those endpoints
     *
     * @param engine the engine for which KOs should be activated.
     * @return set of activated endpoint paths
     */
    @GetMapping(value = "/{engine}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String activateForEngine(@PathVariable String engine) {
        Map<URI, org.kgrid.activator.services.Endpoint> endpointsToActivate = new HashMap<>();
        for (org.kgrid.activator.services.Endpoint endpoint : endpoints.values()) {
            if (engine.equals(endpoint.getEngine())) {
                endpoint.setStatus("GOOD"); // reset status so it can be activated
                endpointsToActivate.put(endpoint.getId(), endpoint);
            }
        }
        activationService.activate(endpointsToActivate);

        endpoints.putAll(endpointsToActivate);
        return getActivationResults(engine).toString();
    }

    /**
     * For A KO remove endpoints, load endpoints, and activate those endpoints
     *
     * @param naan ko naan
     * @param name ko name
     * @return set of activated endpoint paths
     */
    @GetMapping(value = "/{naan}/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String activateKo(@PathVariable String naan,
                             @PathVariable String name) {
        return activateForArkId(naan, name, null);
    }

    /**
     * For an Implementation Remove endpoints, Load endpoints, and activate those endpoints
     *
     * @param naan
     * @param name
     * @param apiVersion
     * @return
     */
    @GetMapping(value = "/{naan}/{name}/{apiVersion}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String activateKoVersion(@PathVariable String naan,
                                    @PathVariable String name, @PathVariable String apiVersion) {
        return activateForArkId(naan, name, apiVersion);
    }

    private String activateForArkId(String naan, String name, String apiVersion) {
        ArkId arkId;
        if (apiVersion == null) {
            arkId = new ArkId(naan, name);
        } else {
            arkId = new ArkId(naan, name, apiVersion);
        }
        log.info("Activate {}", arkId.getSlashArkVersion());

        Map<URI, org.kgrid.activator.services.Endpoint>
                loadedEndpoints = endpointLoader.load(arkId);
        activationService.activate(loadedEndpoints);
        endpoints.putAll(loadedEndpoints);
        JsonArray activatedEndpoints = getActivationResults();
        return activatedEndpoints.toString();
    }

    private JsonArray getActivationResults() {
        JsonArray endpointActivations = new JsonArray();

        endpoints.values().forEach(endpoint -> {
            JsonObject endpointActivationResult = new JsonObject();
            endpointActivationResult.addProperty("@id", "/" + endpoint.getId());
            endpointActivationResult.addProperty("activated", endpoint.getActivated().toString());
            endpointActivationResult.addProperty("status", endpoint.getStatus());
            endpointActivations.add(endpointActivationResult);
        });
        return endpointActivations;
    }

    private JsonArray getActivationResults(String engine) {
        JsonArray endpointActivations = new JsonArray();

        endpoints.values().forEach(endpoint -> {
            if (engine.equals(endpoint.getEngine())) {
                JsonObject endpointActivationResult = new JsonObject();
                endpointActivationResult.addProperty("@id", "/" + endpoint.getId());
                endpointActivationResult.addProperty("activated", endpoint.getActivated().toString());
                endpointActivationResult.addProperty("status", endpoint.getStatus());
                endpointActivations.add(endpointActivationResult);
            }
        });
        return endpointActivations;
    }

}

package org.kgrid.activator.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.kgrid.activator.EndpointLoader;
import org.kgrid.activator.services.ActivationService;
import org.kgrid.activator.services.Endpoint;
import org.kgrid.shelf.domain.ArkId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

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
    public RedirectView activate() {
        log.info("Load and Activate all endpoints ");
        endpoints.clear();
        Map<URI, Endpoint> loadedEndpoints = endpointLoader.load();
        endpoints.putAll(loadedEndpoints);
        activationService.activateEndpoints(endpoints);

        RedirectView redirectView = new RedirectView("/endpoints");
        redirectView.setHttp10Compatible(false);
        return redirectView;
    }

    /**
     * For KOs of a specific engine: remove endpoints, load endpoints, and activate those endpoints
     *
     * @param engine the engine for which KOs should be activated.
     * @return set of activated endpoint paths
     */
    @GetMapping(value = "/{engine}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RedirectView activateForEngine(@PathVariable String engine) {
        Map<URI, org.kgrid.activator.services.Endpoint> endpointsToActivate = new HashMap<>();
        for (org.kgrid.activator.services.Endpoint endpoint : endpoints.values()) {
            if (engine.equals(endpoint.getEngine())) {
                endpoint.setStatus("GOOD"); // reset status so it can be activated
                endpointsToActivate.put(endpoint.getId(), endpoint);
            }
        }
        activationService.activateEndpoints(endpointsToActivate);
        checkForDuplicateEndpoints(endpointsToActivate);
        endpoints.putAll(endpointsToActivate);
        RedirectView redirectView = new RedirectView("/endpoints/" + engine);
        redirectView.setHttp10Compatible(false);
        return redirectView;
    }

    /**
     * For A KO remove endpoints, load endpoints, and activate those endpoints
     *
     * @param naan ko naan
     * @param name ko name
     * @return set of activated endpoint paths
     */
    @GetMapping(value = "/{naan}/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RedirectView activateKo(@PathVariable String naan,
                                   @PathVariable String name) {
        return activateForArkId(naan, name, null);
    }

    /**
     * For an Implementation Remove endpoints, Load endpoints, and activate those endpoints
     *
     * @param naan
     * @param name
     * @param version
     * @return
     */
    @GetMapping(value = "/{naan}/{name}/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RedirectView activateKoVersion(@PathVariable String naan,
                                          @PathVariable String name, @PathVariable String version) {
        return activateForArkId(naan, name, version);
    }

    private RedirectView activateForArkId(String naan, String name, String version) {
        ArkId arkId;
        if (version == null) {
            arkId = new ArkId(naan, name);
        } else {
            arkId = new ArkId(naan, name, version);
        }
        log.info("Activate {}", arkId.getSlashArkVersion());

        Map<URI, org.kgrid.activator.services.Endpoint>
                loadedEndpoints = endpointLoader.load(arkId);
        activationService.activateEndpoints(loadedEndpoints);
        checkForDuplicateEndpoints(loadedEndpoints);
        endpoints.putAll(loadedEndpoints);
        RedirectView redirectView = new RedirectView("/endpoints");
        redirectView.setHttp10Compatible(false);
        return redirectView;
    }

    private void checkForDuplicateEndpoints(Map<URI, Endpoint> loadedEndpoints) {
        for (Map.Entry<URI, Endpoint> entry : loadedEndpoints.entrySet()) {
            if (endpoints.containsKey(entry.getKey())) {
                log.warn(String.format("Overwriting duplicate endpoint: %s", entry.getKey()));
            }
        }
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

}

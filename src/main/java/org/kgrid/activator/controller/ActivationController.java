package org.kgrid.activator.controller;

import org.kgrid.activator.EndpointLoader;
import org.kgrid.activator.services.ActivationService;
import org.kgrid.activator.domain.Endpoint;
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
    private Map<URI, Endpoint> endpoints;

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
        Map<URI, Endpoint> endpointsToActivate = new HashMap<>();
        for (Endpoint endpoint : endpoints.values()) {

        Map<URI, org.kgrid.activator.services.Endpoint> endpointsToActivate = new HashMap<>();
        for (org.kgrid.activator.services.Endpoint endpoint : endpoints.values()) {
            if (engine.equals(endpoint.getEngine())) {
                endpointsToActivate.put(endpoint.getId(), endpoint);
            }
        }

        activationService.activateEndpoints(endpointsToActivate);

        logOverwriteOfExistingEndpoints(endpointsToActivate);
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
     * @param naan naan of the Knowledge object, the first part of the ark
     * @param name name of the Knowledge object, the second part of the ark
     * @param version code version of the Knowledge object, the third part of the ark
     * @return returns a redirect to the activated endpoints
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

        Map<URI, Endpoint>
                loadedEndpoints = endpointLoader.load(arkId);
        activationService.activateEndpoints(loadedEndpoints);
        logOverwriteOfExistingEndpoints(loadedEndpoints);
        endpoints.putAll(loadedEndpoints);
        RedirectView redirectView = new RedirectView("/endpoints");
        redirectView.setHttp10Compatible(false);
        return redirectView;
    }

    private void logOverwriteOfExistingEndpoints(Map<URI, Endpoint> loadedEndpoints) {
        for (Map.Entry<URI, Endpoint> entry : loadedEndpoints.entrySet()) {
            if (endpoints.containsKey(entry.getKey())) {
                log.warn(String.format("Overwriting duplicate endpoint: %s", entry.getKey()));
            }
        }
    }
}

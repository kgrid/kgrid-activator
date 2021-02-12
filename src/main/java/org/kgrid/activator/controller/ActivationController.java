package org.kgrid.activator.controller;

import org.kgrid.activator.services.ActivationService;
import org.kgrid.activator.services.KoLoader;
import org.kgrid.shelf.domain.ArkId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@CrossOrigin
@Primary
public class ActivationController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ActivationService activationService;
    private final KoLoader koLoader;

    public ActivationController(ActivationService activationService, KoLoader koLoader) {
        this.activationService = activationService;
        this.koLoader = koLoader;
    }

    /**
     * Remove all endpoints and load and activate
     *
     * @return set of activated endpoint paths
     */
    @GetMapping(value = "/reload", produces = MediaType.APPLICATION_JSON_VALUE)
    public RedirectView activate() {
        log.info("Load and Activate all endpoints ");

        activationService.activateEndpoints(koLoader.loadAllKos());

        RedirectView redirectView = new RedirectView("/endpoints");
        redirectView.setHttp10Compatible(false);
        return redirectView;
    }

    /**
     * Remove all endpoints and load and activate
     *
     * @return set of activated endpoint paths
     */
    @GetMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public RedirectView refresh() {
        log.info("Activate all endpoints ");

        activationService.reactivateAll();

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
    @GetMapping(value = "/refresh/{engine}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RedirectView refreshForEngine(@PathVariable String engine) {

        activationService.reactivateEngine(engine);
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
    @GetMapping(value = "/reload/{naan}/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RedirectView activateKo(@PathVariable String naan,
                                   @PathVariable String name) {

        return activateForArkId(naan, name, null);
    }

    /**
     * For an Implementation Remove endpoints, Load endpoints, and activate those endpoints
     *
     * @param naan    naan of the Knowledge object, the first part of the ark
     * @param name    name of the Knowledge object, the second part of the ark
     * @param version code version of the Knowledge object, the third part of the ark
     * @return returns a redirect to the activated endpoints
     */
    @GetMapping(value = "/reload/{naan}/{name}/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
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


        activationService.activateEndpoints(koLoader.loadOneKo(arkId));
        RedirectView redirectView = new RedirectView("/endpoints");
        redirectView.setHttp10Compatible(false);
        return redirectView;
    }

}

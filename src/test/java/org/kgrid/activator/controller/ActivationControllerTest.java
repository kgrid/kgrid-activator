package org.kgrid.activator.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kgrid.activator.domain.Endpoint;
import org.kgrid.activator.services.ActivationService;
import org.kgrid.activator.services.KoLoader;
import org.kgrid.shelf.domain.ArkId;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URI;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.kgrid.activator.testUtilities.KoCreationTestHelper.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Activation Controller Tests")
public class ActivationControllerTest {

    @Mock
    private ActivationService activationService;

    @Mock
    private KoLoader koLoader;

    @InjectMocks
    ActivationController activationController;

    Map<URI, Endpoint> endpointMap = new TreeMap<>();

    @BeforeEach
    public void setUp() {
        endpointMap.put(JS_ENDPOINT_URI, getEndpointForEngine(JS_ENGINE));
    }

    @Test
    @DisplayName("Global activation interactions")
    public void testActivateInteractionsAndResult() {

        when(koLoader.loadAllKos()).thenReturn(endpointMap);
        RedirectView redirectView = activationController.reloadAll();
        assertAll(
                () -> verify(koLoader).loadAllKos(),
                () -> verify(activationService).activateEndpointsAndUpdate(endpointMap),
                () -> assertEquals("/endpoints", redirectView.getUrl())
        );
    }

    @Test
    @DisplayName("Engine activation interactions")
    public void testActivateForEngineInteractionsAndResult() {
        RedirectView redirectView = activationController.refreshForEngine(NODE_ENGINE);
        assertAll(
                () -> verify(activationService).activateForEngine(NODE_ENGINE),
                () -> assertEquals("/endpoints/" + NODE_ENGINE, redirectView.getUrl())
        );
    }

    @Test
    @DisplayName("Single ko activation interactions")
    public void testActivateKoInteractionsAndResult() {
        ArkId arkId = new ArkId(NODE_NAAN, NODE_NAME, NODE_VERSION);
        when(koLoader.loadOneKo(arkId)).thenReturn(endpointMap);
        RedirectView redirectView = activationController.reloadKoWithVersionParam(NODE_NAAN, NODE_NAME, NODE_VERSION);
        assertAll(
                () -> verify(koLoader).loadOneKo(arkId),
                () -> verify(activationService).activateEndpointsAndUpdate(endpointMap),
                () -> assertEquals("/endpoints", redirectView.getUrl())
        );
    }

    @Test
    @DisplayName("Single ko version activation interactions")
    public void testActivateKoVersionInteractionsAndResult() {
        ArkId arkId = new ArkId(NODE_NAAN, NODE_NAME, NODE_VERSION);
        when(koLoader.loadOneKo(arkId)).thenReturn(endpointMap);
        RedirectView redirectView = activationController.reloadKo(NODE_NAAN, NODE_NAME, NODE_VERSION);
        assertAll(
                () -> verify(koLoader).loadOneKo(arkId),
                () -> verify(activationService).activateEndpointsAndUpdate(endpointMap),
                () -> assertEquals("/endpoints", redirectView.getUrl())
        );
    }
}

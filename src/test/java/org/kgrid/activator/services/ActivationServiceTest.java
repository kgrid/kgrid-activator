package org.kgrid.activator.services;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kgrid.activator.constants.EndpointStatus;
import org.kgrid.activator.domain.EndPointResult;
import org.kgrid.activator.domain.Endpoint;
import org.kgrid.activator.testUtilities.KoCreationTestHelper;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.Executor;
import org.mockito.BDDMockito.Then;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.kgrid.activator.testUtilities.KoCreationTestHelper.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@ExtendWith(MockitoExtension.class)
@DisplayName("Activation Service Tests")
public class ActivationServiceTest {

    public static final URI OBJECT_LOCATION = URI.create("ObjectLocation");
    private final Map<URI, Endpoint> endpointMap = new HashMap<>();
    private Endpoint endpoint;
    @Mock
    private Adapter adapter;
    @Mock
    private Executor executor;
    private ActivationService activationService;
    private JsonNode deploymentJson;

    @BeforeEach
    public void setup() {
        List<Adapter> adapters = new ArrayList<>();
        adapters.add(adapter);
        activationService = new ActivationService();
        activationService.setAdapters(adapters);
        deploymentJson = getEndpointDeploymentJsonForEngine(JS_ENGINE, JS_ENDPOINT_NAME);
        JsonNode metadata = generateMetadata(JS_NAAN, JS_NAME, JS_VERSION);
        EndPointResult<Object> endPointResult = new EndPointResult<>(null);
        String input = "input";
        endPointResult.getInfo().put("inputs", input);
        endPointResult.getInfo().put("ko", metadata);
        endpoint = getEndpointForEngine(JS_ENGINE);
        endpointMap.put(JS_ENDPOINT_URI, endpoint);
        List<String> engines = new ArrayList<>();
        engines.add(JS_ENGINE);
        when(adapter.getEngines()).thenReturn(engines);
    }

    @Test
    @DisplayName("Activate Works Successfully")
    public void activateCreatesEndpointWithExecutor() {
        when(adapter.activate(any(), any(), any())).thenReturn(executor);

        activationService.activateEndpointsAndUpdate(endpointMap);
        assertAll(
                () -> verify(adapter).activate(OBJECT_LOCATION, JS_ENDPOINT_URI,
                        deploymentJson.get("/" + JS_ENDPOINT_NAME).get(POST_HTTP_METHOD)),
                () -> assertEquals(executor, endpoint.getExecutor()),
                () -> assertEquals(EndpointStatus.ACTIVATED.name(), endpoint.getStatus())
        );
    }

    @Test
    @DisplayName("Activate does not create executor after error")
    public void activateDoesNotSetExecutorIfActivatorExceptionIsThrownAnywhere() {
        when(adapter.activate(any(), any(), any())).thenThrow(new AdapterException(""));
        endpoint.getWrapper().addDeployment(null);
        activationService.activateEndpointsAndUpdate(endpointMap);
        assertNull(endpoint.getExecutor());
    }

    @Test
    @DisplayName("Activate handles shelf exception")
    public void activateCatchesExceptionsFromShelf() {
        activationService.activateEndpointsAndUpdate(endpointMap);
        assertNull(endpoint.getExecutor());
    }

    @Test
    @DisplayName("Activate handles adapter exception")
    public void activateCatchesExceptionsFromAdapter() {
        String exceptionMessage = "ope";
        when(adapter.activate(any(), any(), any())).thenThrow(new AdapterException(exceptionMessage));
        activationService.activateEndpointsAndUpdate(endpointMap);
        assertAll(
                () -> assertEquals(EndpointStatus.FAILED_TO_ACTIVATE.name(), endpoint.getStatus()),
                () -> assertEquals(String.format("Could not activate %s. Cause: %s",
                        KoCreationTestHelper.JS_ENDPOINT_ID, exceptionMessage), endpoint.getDetail())
        );
    }

    @Test
    @DisplayName("Activate fails when no engine is found")
    public void activateFailsNoEngine() {
        when(adapter.getEngines()).thenReturn(new ArrayList<>());
        activationService.activateEndpointsAndUpdate(endpointMap);
        assertAll(
                () -> assertEquals(EndpointStatus.FAILED_TO_ACTIVATE.name(), endpoint.getStatus()),
                () -> assertEquals("Could not activate naan/name/jsApiVersion/endpoint. Cause: No adapter loaded for engine javascript", endpoint.getDetail())
        );

    }

    @Test
    void getAllVersionReturnsCorrectSubset() {
        // Given
        Map<URI, Endpoint> eps = List.of(
            createMockEndpoint(URI.create("naan/name/1.0/test1"), true),
            createMockEndpoint(URI.create("naan/name/1.3/test1"), true),
            createMockEndpoint(URI.create("naan/name/1.0/test2"), true),
            createMockEndpoint(URI.create("naan/name/1.1/test1"), true)
        ).stream()
            .collect(Collectors.toMap(e -> e.getId(), e -> e));

        activationService.activateEndpointsAndUpdate(eps);
        assertEquals(eps.size(), activationService.getEndpoints().size());

        // when
        final List<Endpoint> allVersions = activationService
            .getAllVersions("naan", "name", "test1");

        // then
        assertEquals(3, allVersions.size());
    }

    // just creating stubs with a variety of combinations; not testing Endpoints
    private static Endpoint createMockEndpoint(URI id, boolean isActive) {
        Endpoint ep = mock(Endpoint.class, withSettings().lenient());
        final String[] split = id.getPath().split("/");
        when(ep.equalsIgnoreVersion(split[0], split[1],split[3])).thenReturn(true);
        when(ep.getApiVersion()).thenReturn(split[2]);
        when(ep.getEndpointName()).thenReturn(split[3]);
        when(ep.getId()).thenReturn(id);
        when(ep.isActive()).thenReturn(isActive);
        return ep;
    }

}

package org.kgrid.activator.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kgrid.activator.domain.Endpoint;
import org.kgrid.activator.services.ActivationService;
import org.kgrid.activator.testUtilities.KoCreationTestHelper;
import org.kgrid.shelf.domain.KnowledgeObjectWrapper;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@DisplayName("Shelf Interceptor Tests")
public class ShelfInterceptorTest {

    private ActivationService activationService = Mockito.mock(ActivationService.class);
    private Map<URI, Endpoint> globalEndpoints = new HashMap<>();

    ShelfInterceptor shelfInterceptor;
    MockHttpServletRequest request;
    MockHttpServletResponse response;
    MockFilterChain filterChain;

    @BeforeEach
    public void setUp() {
        request = new MockHttpServletRequest();
        request.setMethod("DELETE");
        request.setRequestURI("/kos/naan/name/version");
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        when(activationService.getEndpointMap()).thenReturn(globalEndpoints);
        shelfInterceptor = new ShelfInterceptor(activationService);
        org.springframework.test.util.ReflectionTestUtils.setField(shelfInterceptor, "shelfEndpoint", "kos");
    }

    @Test
    @DisplayName("Removes endpoint when deleted")
    public void removesEndpointFromMapWhenDeleted() throws IOException, ServletException {
        Endpoint endpoint = new Endpoint(new KnowledgeObjectWrapper(
                KoCreationTestHelper.generateMetadata("naan", "name", "version")),
                "endpoint");
        globalEndpoints.put(URI.create("naan/name/version/endpoint/"), endpoint);
        assertEquals(1, globalEndpoints.size());
        shelfInterceptor.doFilter(request, response, filterChain);
        assertEquals(0, globalEndpoints.size());

    }

    @Test
    @DisplayName("Does not remove unspecified endpoint")
    public void doesNotRemoveEndpointFromMapWhenOtherDeleted() throws IOException, ServletException {
        Endpoint endpoint = new Endpoint(new KnowledgeObjectWrapper(
                KoCreationTestHelper.generateMetadata("naan", "name", "version2")),
                "endpoint");
        globalEndpoints.put(URI.create("naan/name/version/endpoint/"), endpoint);
        assertEquals(1, globalEndpoints.size());
        shelfInterceptor.doFilter(request, response, filterChain);
        assertEquals(1, globalEndpoints.size());

    }
}
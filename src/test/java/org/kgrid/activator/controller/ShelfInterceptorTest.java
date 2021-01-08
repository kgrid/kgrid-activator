package org.kgrid.activator.controller;

import org.junit.Before;
import org.junit.Test;
import org.kgrid.activator.services.Endpoint;
import org.kgrid.activator.utils.KoCreationTestHelper;
import org.kgrid.shelf.domain.KnowledgeObjectWrapper;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShelfInterceptorTest {

    private Map<URI, Endpoint> globalEndpoints = new HashMap<>();

    ShelfInterceptor shelfInterceptor;
    MockHttpServletRequest request;
    MockHttpServletResponse response;
    MockFilterChain filterChain;

    @Before
    public void setUp() {
        request = new MockHttpServletRequest();
        request.setMethod("DELETE");
        request.setRequestURI("/kos/naan/name/version");
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();

        shelfInterceptor = new ShelfInterceptor(globalEndpoints);
        org.springframework.test.util.ReflectionTestUtils.setField(shelfInterceptor, "shelfEndpoint", "kos");
    }

    @Test
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
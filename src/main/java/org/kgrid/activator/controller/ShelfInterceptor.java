package org.kgrid.activator.controller;

import org.apache.commons.lang3.StringUtils;
import org.kgrid.activator.services.Endpoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ShelfInterceptor implements Filter {

    final Map<URI, Endpoint> endpointMap;

    @Value("${kgrid.shelf.endpoint:kos}")
    String shelfEndpoint;

    public ShelfInterceptor(Map<URI, Endpoint> endpointMap) {
        this.endpointMap = endpointMap;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        if (request.getMethod().equalsIgnoreCase(HttpMethod.DELETE.name())) {
            String deletedId = StringUtils.substringAfter(request.getRequestURI(), shelfEndpoint + "/") + "/";

            List<URI> removedEndpoints = new ArrayList<>();
            endpointMap.forEach((uri, endpoint) -> {
                if (endpoint.getWrapper().getId().toString().equals(deletedId)) {
                    removedEndpoints.add(uri);
                }
            });
            removedEndpoints.forEach(endpoint -> {
                endpointMap.remove(endpoint);
            });

        }
        filterChain.doFilter(request, servletResponse);
    }
}

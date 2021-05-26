package org.kgrid.activator.controller;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.kgrid.activator.domain.Endpoint;
import org.kgrid.activator.services.ActivationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
public class ShelfInterceptor implements Filter {

    private final ActivationService activationService;

    @Value("/kos")
    String shelfEndpoint;

    public ShelfInterceptor(ActivationService activationService) {
        this.activationService = activationService;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        if (request.getMethod().equalsIgnoreCase(HttpMethod.DELETE.name())) {
            String deletedKoId = StringUtils.substringAfter(request.getRequestURI(), shelfEndpoint + "/") + "/";


            activationService.getEndpoints().forEach((endpoint) -> {
                    if (endpoint.getWrapper().getId().toString().equals(deletedKoId)) {
                        activationService.remove(endpoint);
                    }
                }
            );
        }
        filterChain.doFilter(request, servletResponse);
    }
}

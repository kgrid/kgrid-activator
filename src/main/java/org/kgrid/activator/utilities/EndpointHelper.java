package org.kgrid.activator.utilities;

import org.apache.commons.lang3.StringUtils;
import org.kgrid.activator.exceptions.ActivatorEndpointNotFoundException;
import org.kgrid.activator.domain.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.activation.MimetypesFileTypeMap;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class EndpointHelper {

    @Autowired
    private Map<URI, Endpoint> endpoints;

    @Autowired
    private MimetypesFileTypeMap fileTypeMap;

    public String getDefaultVersion(String naan, String name, String endpoint) {
        final List<Endpoint> allVersions = getAllVersions(naan, name, endpoint);
        Collections.sort(allVersions);
        return allVersions.get(0).getApiVersion();
    }

    public List<Endpoint> getAllVersions(String naan, String name, String endpoint) {
        List<Endpoint> versions = new ArrayList<>();
        for (Map.Entry<URI, Endpoint> entry : endpoints.entrySet()) {
            if (entry.getValue().getNaan().equals(naan)
                    && entry.getValue().getName().equals(name)
                    && entry.getValue().getEndpointName().equals(endpoint)) {
                versions.add(entry.getValue());
            }
        }
        if (versions.isEmpty()) {
            throw new ActivatorEndpointNotFoundException(String.format("No active endpoints found for %s/%s/%s",
                    naan, name, endpoint));
        }
        return versions;
    }

    public String getContentType(String artifactName) {
        return fileTypeMap.getContentType(artifactName);
    }

    public String getContentDisposition(String artifactName) {
        String filename =
                artifactName.contains("/") ? StringUtils.substringAfterLast(artifactName, "/") : artifactName;
        return "inline; filename=\"" + filename + "\"";
    }

    public URI createEndpointId(String naan, String name, String apiVersion, String endpoint) {
        if (apiVersion == null) {
            apiVersion = getDefaultVersion(naan, name, endpoint);
        }
        return URI.create(String.format("%s/%s/%s/%s", naan, name, apiVersion, endpoint));
    }

    public Endpoint getEndpoint(URI endpointId) {
        return endpoints.get(endpointId);
    }
}

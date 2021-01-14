package org.kgrid.activator.Utilities;

import org.apache.commons.lang3.StringUtils;
import org.kgrid.activator.EndPointResult;
import org.kgrid.activator.exceptions.ActivatorEndpointNotFoundException;
import org.kgrid.activator.exceptions.ActivatorUnsupportedMediaTypeException;
import org.kgrid.activator.services.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.activation.MimetypesFileTypeMap;
import java.net.URI;
import java.util.Map;

@Service
public class EndpointHelper {

    @Autowired
    private Map<URI, Endpoint> endpoints;

    @Autowired
    private MimetypesFileTypeMap fileTypeMap;

    public String getDefaultVersion(String naan, String name, String endpoint) {
        String defaultVersion = null;
        for (Map.Entry<URI, Endpoint> entry : endpoints.entrySet()) {
            if (entry.getValue().getNaan().equals(naan)
                    && entry.getValue().getName().equals(name)
                    && entry.getValue().getEndpointName().equals(endpoint)) {

                defaultVersion = entry.getValue().getApiVersion();
                break;
            }
        }
        return defaultVersion;
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
        return URI.create(String.format("%s/%s/%s/%s", naan, name, apiVersion, endpoint));
    }

    public Endpoint getEndpoint(URI endpointId){
        return endpoints.get(endpointId);
    }
}

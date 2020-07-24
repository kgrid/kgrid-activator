package org.kgrid.activator.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.kgrid.activator.ActivatorException;
import org.springframework.stereotype.Service;

@Service
public class KoValidationService {

    public static final String HAS_MISSING_SERVICE_SPECIFICATION = "Has missing Service Specification";
    public static final String HAS_MISSING_DEPLOYMENT_SPECIFICATION = "Has missing Deployment Specification";
    public static final String HAS_MISSING_PATHS = "Has missing paths node in Service Specification";
    public static final String HAS_NO_DEFINED_PATHS = "Has an empty Paths node in Service Specification";
    public static final String HAS_NO_ARTIFACT_IN_DEPLOYMENT_SPECIFICATION = "Has no defined artifact in Deployment Specification";
    public static final String HAS_NO_ADAPTER_IN_DEPLOYMENT_SPECIFICATION = "Has no defined adapter in Deployment Specification";


    public void validateActivatability(JsonNode serviceSpec, JsonNode deploymentSpec) {
        JsonNode pathNode = serviceSpec.at("/paths");
        pathNode.fields().forEachRemaining(path -> {
            path.getValue().fields().forEachRemaining(httpMethod -> {
                JsonNode xKgridActivationNode = httpMethod.getValue().at("/x-kgrid-activation");
                if (xKgridActivationNode.isMissingNode()) {
                    validateDeploymentSpecification(deploymentSpec, path.getKey());
                }
            });
        });
    }

    public void validateMetadata(JsonNode koMetadata) {
        JsonNode hasServiceSpecification = koMetadata.at("/hasServiceSpecification");
        JsonNode hasDeploymentSpecification = koMetadata.at("/hasDeploymentSpecification");
        if (hasServiceSpecification.isMissingNode())
            throwWithMessage(HAS_MISSING_SERVICE_SPECIFICATION);
        if (hasDeploymentSpecification.isMissingNode())
            throwWithMessage(HAS_MISSING_DEPLOYMENT_SPECIFICATION);
    }

    public void validateServiceDescription(JsonNode serviceSpecification) {
        if (serviceSpecification.at("/paths").isMissingNode())
            throwWithMessage(HAS_MISSING_PATHS);
        if (serviceSpecification.at("/paths").size() == 0)
            throwWithMessage(HAS_NO_DEFINED_PATHS);
    }

    public void validateDeploymentSpecification(JsonNode deploymentSpecification, String pathName) {
        JsonNode endpointNode = deploymentSpecification.at("/endpoints/~1" + pathName.substring(1));
        if (endpointNode.has("artifact")) {
            if (!endpointNode.has("adapter"))
                throwWithMessage(HAS_NO_ADAPTER_IN_DEPLOYMENT_SPECIFICATION);
        } else {
            throwWithMessage(HAS_NO_ARTIFACT_IN_DEPLOYMENT_SPECIFICATION);
        }
    }

    private void throwWithMessage(String message) {
        throw new ActivatorException(message);
    }
}

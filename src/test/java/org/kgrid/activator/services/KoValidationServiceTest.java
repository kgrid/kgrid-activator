package org.kgrid.activator.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kgrid.activator.ActivatorException;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class KoValidationServiceTest {

    @InjectMocks
    KoValidationService koValidationService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void validateMetadata_validMetadataPassesValidation() throws JsonProcessingException {
        JsonNode metadata = objectMapper.createObjectNode()
                .put("hasServiceSpecification", "value")
                .put("hasDeploymentSpecification", "value");
        koValidationService.validateMetadata(metadata);
    }

    @Test
    public void validateMetadata_metadataMissingHasServiceDescriptionThrowsException() {
        JsonNode metadata = objectMapper.createObjectNode();

        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> koValidationService.validateMetadata(metadata));
        assertEquals(KoValidationService.HAS_MISSING_SERVICE_SPECIFICATION, activatorException.getMessage());
    }

    @Test
    public void validateMetadata_metadataMissingHasDeploymentSpecificationThrowsException() {
        JsonNode metadata = objectMapper.createObjectNode()
                .put("hasServiceSpecification", "value");
        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> koValidationService.validateMetadata(metadata));
        assertEquals(KoValidationService.HAS_MISSING_DEPLOYMENT_SPECIFICATION, activatorException.getMessage());
    }

    @Test
    public void validateServiceSpecification_validServiceSpecPassesValidation() {
        ObjectNode serviceSpec = objectMapper.createObjectNode();
        serviceSpec.putArray("paths").add("tyvkh");
        koValidationService.validateServiceSpecification(serviceSpec);
    }

    @Test
    public void validateServiceSpecification_MissingPathsThrowsException() {
        ObjectNode serviceSpec = objectMapper.createObjectNode();

        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> koValidationService.validateServiceSpecification(serviceSpec));
        assertEquals(KoValidationService.HAS_MISSING_PATHS, activatorException.getMessage());
    }

    @Test
    public void validateServiceSpecification_PresentButEmptyPathsThrowsException() {
        ObjectNode serviceSpec = objectMapper.createObjectNode();
        serviceSpec.putArray("paths");

        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> koValidationService.validateServiceSpecification(serviceSpec));
        assertEquals(KoValidationService.HAS_NO_DEFINED_PATHS, activatorException.getMessage());
    }

    @Test
    public void validateActivatability_KoHasDeploymentInServiceSpec() throws JsonProcessingException {
        ObjectNode serviceSpec = objectMapper.createObjectNode();
        ObjectNode deploymentSpec = objectMapper.createObjectNode();
        JsonNode paths = objectMapper.readTree("{\"/endpoint\":{\"post\":{\"x-kgrid-activation\":\"value\"}}}");
        serviceSpec.set("paths", paths);
        koValidationService.validateActivatability(serviceSpec, deploymentSpec);
    }

    @Test
    public void validateActivatability_KoHasDeploymentInDeploymentSpec() throws JsonProcessingException {
        ObjectNode serviceSpec = objectMapper.createObjectNode();
        JsonNode paths = objectMapper.readTree("{\"/endpoint\":{\"post\":{\"stuff\":\"things\"}}}");
        serviceSpec.set("paths", paths);
        ObjectNode deploymentSpec = objectMapper.createObjectNode()
                .set("endpoints", objectMapper.readTree(
                        "{\"/endpoint\":{\"artifact\":\"Arty McFacts\",\"adapter\":\"V8\",\"function\":\"doorway\"}}"));
        koValidationService.validateActivatability(serviceSpec, deploymentSpec);
    }

    @Test
    public void validateActivatability_KoHasNoArtifactInDeploymentSpec() throws JsonProcessingException {
        ObjectNode serviceSpec = objectMapper.createObjectNode();
        JsonNode paths = objectMapper.readTree("{\"/endpoint\":{\"post\":{\"stuff\":\"things\"}}}");
        serviceSpec.set("paths", paths);
        ObjectNode deploymentSpec = objectMapper.createObjectNode()
                .set("endpoints", objectMapper.readTree(
                        "{\"/endpoint\":{}}"));
        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> koValidationService.validateActivatability(serviceSpec, deploymentSpec));
        assertEquals(KoValidationService.HAS_NO_ARTIFACT_IN_DEPLOYMENT_SPECIFICATION, activatorException.getMessage());
    }

    @Test
    public void validateActivatability_KoHasNoAdapterInDeploymentSpec() throws JsonProcessingException {
        ObjectNode serviceSpec = objectMapper.createObjectNode();
        JsonNode paths = objectMapper.readTree("{\"/endpoint\":{\"post\":{\"stuff\":\"things\"}}}");
        serviceSpec.set("paths", paths);
        ObjectNode deploymentSpec = objectMapper.createObjectNode()
                .set("endpoints", objectMapper.readTree(
                        "{\"/endpoint\":{\"artifact\":\"Arty McFacts\"}}"));
        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> koValidationService.validateActivatability(serviceSpec, deploymentSpec));
        assertEquals(KoValidationService.HAS_NO_ADAPTER_IN_DEPLOYMENT_SPECIFICATION, activatorException.getMessage());
    }

}
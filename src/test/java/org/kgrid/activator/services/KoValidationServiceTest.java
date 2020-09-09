package org.kgrid.activator.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kgrid.activator.ActivatorException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KoValidationServiceTest {

    @InjectMocks
    KoValidationService koValidationService;

    @Mock
    AdapterLoader adapterLoader;

    @Mock
    AdapterResolver adapterResolver;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        Map adapters = new HashMap();
        adapters.put("V8", "");

        when(adapterLoader.getAdapterResolver()).thenReturn(adapterResolver);
        when(adapterResolver.getAdapters()).thenReturn(adapters);
    }

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
        JsonNode paths = objectMapper.readTree("{\"/endpoint\":{\"post\":{\"x-kgrid-activation\":\"value\"}}}");
        serviceSpec.set("paths", paths);
        koValidationService.validateActivatability("/endpoint", serviceSpec, null);
    }

    @Test
    public void validateActivatability_KoHasDeploymentInServiceSpecAndDeploymentSpec() throws JsonProcessingException {
        ObjectNode serviceSpec = objectMapper.createObjectNode();
        JsonNode paths = objectMapper.readTree("{\"/endpoint\":{\"post\":{\"x-kgrid-activation\":\"value\"}}}");
        serviceSpec.set("paths", paths);
        ObjectNode deploymentSpec = objectMapper.createObjectNode()
                .set("endpoints", objectMapper.readTree(
                        "{\"/endpoint\":{\"artifact\":\"Arty McFacts\",\"adapter\":\"V8\",\"function\":\"doorway\"}}"));
        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> koValidationService.validateActivatability("/endpoint", serviceSpec, deploymentSpec));
        assertEquals(KoValidationService.HAS_BOTH_DEPLOYMENT_SPECIFICATION_AND_X_KGRID, activatorException.getMessage());

    }


    @Test
    public void validateActivatability_KoHasDeploymentInDeploymentSpec() throws JsonProcessingException {
        ObjectNode serviceSpec = objectMapper.createObjectNode();
        JsonNode paths = objectMapper.readTree("{\"/endpoint\":{\"post\":{\"stuff\":\"things\"}}}");
        serviceSpec.set("paths", paths);
        ObjectNode deploymentSpec = objectMapper.createObjectNode()
                .set("endpoints", objectMapper.readTree(
                        "{\"/endpoint\":{\"artifact\":\"Arty McFacts\",\"adapter\":\"V8\",\"function\":\"doorway\"}}"));
        koValidationService.validateActivatability("/endpoint", serviceSpec, deploymentSpec);
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
                () -> koValidationService.validateActivatability("/endpoint", serviceSpec, deploymentSpec));
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
                () -> koValidationService.validateActivatability("/endpoint", serviceSpec, deploymentSpec));
        assertEquals(KoValidationService.HAS_NO_ADAPTER_IN_DEPLOYMENT_SPECIFICATION, activatorException.getMessage());
    }

    @Test
    public void validateActivatability_KoHasNoArtifactsDefinedInDeploymentSpec() throws JsonProcessingException {
        ObjectNode serviceSpec = objectMapper.createObjectNode();
        JsonNode paths = objectMapper.readTree("{\"/endpoint\":{\"post\":{\"stuff\":\"things\"}}}");
        serviceSpec.set("paths", paths);
        ObjectNode deploymentSpec = objectMapper.createObjectNode()
                .set("endpoints", objectMapper.readTree(
                        "{\"/endpoint\":{\"artifact\":\"\"}}"));
        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> koValidationService.validateActivatability("/endpoint", serviceSpec, deploymentSpec));
        assertEquals(KoValidationService.HAS_NO_DEFINED_ARTIFACTS_IN_DEPLOYMENT_SPECIFICATION, activatorException.getMessage());
    }

    @Test
    public void validateActivatability_KoHasArtifactArrayDefinedInDeploymentSpec() throws JsonProcessingException {
        ObjectNode serviceSpec = objectMapper.createObjectNode();
        JsonNode paths = objectMapper.readTree("{\"/endpoint\":{\"post\":{\"stuff\":\"things\"}}}");
        serviceSpec.set("paths", paths);
        ObjectNode deploymentSpec = objectMapper.createObjectNode()
                .set("endpoints", objectMapper.readTree(
                        "{\"/endpoint\":{\"artifact\":[\"thingOne.js\",\"thingTwo.js\"],\"adapter\":\"V8\"}}"));
        koValidationService.validateActivatability("/endpoint", serviceSpec, deploymentSpec);
    }

    @Test
    public void validateActivatability_KoHasNoEndpointsDefinedInDeploymentSpec() throws JsonProcessingException {
        ObjectNode serviceSpec = objectMapper.createObjectNode();
        JsonNode paths = objectMapper.readTree("{\"/endpoint\":{\"post\":{\"stuff\":\"things\"}}}");
        serviceSpec.set("paths", paths);
        ObjectNode deploymentSpec = objectMapper.createObjectNode()
                .set("endpoints", objectMapper.readTree(
                        "{}"));
        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> koValidationService.validateActivatability("/endpoint", serviceSpec, deploymentSpec));
        assertEquals(KoValidationService.HAS_NO_ENDPOINTS_DEFINED_IN_DEPLOYMENT_SPECIFICATION, activatorException.getMessage());
    }

    @Test
    public void validateActivatability_KoUsesUnloadedAdapterInDeploymentSpec() throws JsonProcessingException {
        ObjectNode serviceSpec = objectMapper.createObjectNode();
        JsonNode paths = objectMapper.readTree("{\"/endpoint\":{\"post\":{\"stuff\":\"things\"}}}");
        serviceSpec.set("paths", paths);
        ObjectNode deploymentSpec = objectMapper.createObjectNode()
                .set("endpoints", objectMapper.readTree(
                        "{\"/endpoint\":{\"artifact\":[\"thingOne.js\",\"thingTwo.js\"],\"adapter\":\"cool\"}}"));
        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> koValidationService.validateActivatability("/endpoint", serviceSpec, deploymentSpec));
        assertEquals("cool" + KoValidationService.ADAPTER_NOT_AVAILABLE, activatorException.getMessage());
    }


}
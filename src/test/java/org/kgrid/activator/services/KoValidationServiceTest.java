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
import org.kgrid.shelf.domain.KnowledgeObjectWrapper;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class KoValidationServiceTest {

    @InjectMocks
    KoValidationService koValidationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        Map adapters = new HashMap();
        adapters.put("JAVASCRIPT", "");
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
        JsonNode serviceSpec =
                objectMapper.readTree(
                        "{\"paths\":{\"/endpoint\":{\"post\":{\"x-kgrid-activation\":\"value\"}}}}");
        JsonNode deploymentSpec = objectMapper.createObjectNode();
        Endpoint endpoint = getEndpoint(serviceSpec, deploymentSpec);
        koValidationService.validateEndpoint(endpoint);
    }

    private Endpoint getEndpoint(JsonNode serviceSpec, JsonNode deploymentSpec) {
        JsonNode metadata = objectMapper.createObjectNode().put("@id", "naan/name/version");
        KnowledgeObjectWrapper wrapper = new KnowledgeObjectWrapper(metadata);
        wrapper.addDeployment(deploymentSpec);
        wrapper.addService(serviceSpec);
        return new Endpoint(wrapper, "endpoint");
    }

    @Test
    public void validateActivatability_KoHasDeploymentInServiceSpecAndDeploymentSpec() throws JsonProcessingException {
        JsonNode serviceSpec =
                objectMapper.readTree(
                        "{\"paths\":{\"/endpoint\":{\"post\":{\"x-kgrid-activation\":\"value\"}}}}");
        JsonNode deploymentSpec =
                objectMapper.readTree(
                        "{\"endpoints\":{\"/endpoint\":{\"artifact\":\"Arty McFacts\",\"engine\":\"javascript\",\"function\":\"doorway\"}}}");
        Endpoint endpoint = getEndpoint(serviceSpec, deploymentSpec);
        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> koValidationService.validateEndpoint(endpoint));
        assertEquals(KoValidationService.HAS_BOTH_DEPLOYMENT_SPECIFICATION_AND_X_KGRID, activatorException.getMessage());
    }


    @Test
    public void validateActivatability_KoHasDeploymentInDeploymentSpec() throws JsonProcessingException {
        JsonNode serviceSpec = objectMapper.readTree("{\"paths\":{\"/endpoint\":{\"post\":{\"stuff\":\"things\"}}}}");
        JsonNode deploymentSpec =
                objectMapper.readTree(
                        "{\"/endpoint\":{\"post\":{\"artifact\":\"Arty McFacts\",\"engine\":\"javascript\",\"function\":\"doorway\"}}}");

        Endpoint endpoint = getEndpoint(serviceSpec, deploymentSpec);
        koValidationService.validateEndpoint(endpoint);
    }

    @Test
    public void validateActivatability_KoHasNoArtifactInDeploymentSpec() throws JsonProcessingException {
        JsonNode serviceSpec = objectMapper.readTree("{\"paths\":{\"/endpoint\":{\"post\":{\"stuff\":\"things\"}}}}");
        JsonNode deploymentSpec = objectMapper.readTree("{\"endpoints\":{\"/endpoint\":{}}}");
        Endpoint endpoint = getEndpoint(serviceSpec, deploymentSpec);
        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> koValidationService.validateEndpoint(endpoint));
        assertEquals(KoValidationService.HAS_NO_ARTIFACT_IN_DEPLOYMENT_SPECIFICATION, activatorException.getMessage());
    }

    @Test
    public void validateActivatability_KoHasNoAdapterInDeploymentSpec() throws JsonProcessingException {
        JsonNode serviceSpec = objectMapper.readTree("{\"paths\":{\"/endpoint\":{\"post\":{\"stuff\":\"things\"}}}}");
        JsonNode deploymentSpec =
                objectMapper.readTree(
                        "{\"/endpoint\":{\"post\":{\"artifact\":\"Arty McFacts\"}}}");
        Endpoint endpoint = getEndpoint(serviceSpec, deploymentSpec);
        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> koValidationService.validateEndpoint(endpoint));
        assertEquals(KoValidationService.HAS_NO_ADAPTER_IN_DEPLOYMENT_SPECIFICATION, activatorException.getMessage());
    }

    @Test
    public void validateActivatability_KoHasNoArtifactsDefinedInDeploymentSpec() throws JsonProcessingException {
        JsonNode serviceSpec = objectMapper.readTree("{\"paths\":{\"/endpoint\":{\"post\":{\"stuff\":\"things\"}}}}");
        JsonNode deploymentSpec = objectMapper.readTree("{\"/endpoint\":{\"post\":{\"artifact\":\"\"}}}");

        Endpoint endpoint = getEndpoint(serviceSpec, deploymentSpec);
        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> koValidationService.validateEndpoint(endpoint));
        assertEquals(KoValidationService.HAS_NO_DEFINED_ARTIFACTS_IN_DEPLOYMENT_SPECIFICATION, activatorException.getMessage());
    }

    @Test
    public void validateActivatability_KoHasArtifactArrayDefinedInDeploymentSpec() throws JsonProcessingException {
        JsonNode serviceSpec = objectMapper.readTree("{\"paths\":{\"/endpoint\":{\"post\":{\"stuff\":\"things\"}}}}");
        JsonNode deploymentSpec = objectMapper.readTree("{\"/endpoint\":{\"post\":{\"artifact\":[\"thingOne.js\",\"thingTwo.js\"],\"engine\":\"javascript\"}}}");
        Endpoint endpoint = getEndpoint(serviceSpec, deploymentSpec);
        koValidationService.validateEndpoint(endpoint);
    }

    @Test
    public void validateActivatability_KoHasNoEndpointsDefinedInDeploymentSpec() throws JsonProcessingException {
        JsonNode serviceSpec = objectMapper.readTree("{\"paths\":{\"/endpoint\":{\"post\":{\"stuff\":\"things\"}}}}");
        JsonNode deploymentSpec = objectMapper.readTree("{}");
        Endpoint endpoint = getEndpoint(serviceSpec, deploymentSpec);
        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> koValidationService.validateEndpoint(endpoint));
        assertEquals(KoValidationService.HAS_NO_ENDPOINTS_DEFINED_IN_DEPLOYMENT_SPECIFICATION, activatorException.getMessage());
    }
}
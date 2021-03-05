package org.kgrid.activator.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kgrid.activator.domain.Endpoint;
import org.kgrid.activator.exceptions.ActivatorException;
import org.kgrid.shelf.domain.KnowledgeObjectWrapper;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class KoValidationServiceTest {

    @InjectMocks
    KoValidationService koValidationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void validateMetadata_validMetadataPassesValidation() {
        JsonNode metadata = objectMapper.createObjectNode()
                .put("hasServiceSpecification", "value")
                .put("hasDeploymentSpecification", "value");
        koValidationService.validateMetadata(metadata);
    }

    @Test
    public void validateMetadata_throwsHttpStatusUnprocessable() {
        JsonNode metadata = objectMapper.createObjectNode();

        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> koValidationService.validateMetadata(metadata));
        assertEquals("Has missing Service Specification", activatorException.getMessage());
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
        assertEquals(KoValidationService.HAS_MISSING_PATHS_NODE_IN_SERVICE_SPECIFICATION, activatorException.getMessage());
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
        JsonNode deploymentSpec = objectMapper.readTree("{\"endpoints\":{\"/endpoint\":{}}}");
        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> koValidationService.validateDeploymentSpecification(deploymentSpec));
        assertEquals(KoValidationService.HAS_NO_ARTIFACT_IN_DEPLOYMENT_SPECIFICATION, activatorException.getMessage());
    }

    @Test
    public void validateActivatability_KoHasNoAdapterInDeploymentSpec() throws JsonProcessingException {
        JsonNode deploymentSpec =
                objectMapper.readTree(
                        "{\"/endpoint\":{\"post\":{\"artifact\":\"Arty McFacts\"}}}");
        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> koValidationService.validateDeploymentSpecification(deploymentSpec));
        assertEquals(KoValidationService.HAS_NO_ENGINE_IN_DEPLOYMENT_SPECIFICATION, activatorException.getMessage());
    }

    @Test
    public void validateActivatability_KoHasNoArtifactsDefinedInDeploymentSpec() throws JsonProcessingException {
        JsonNode deploymentSpec = objectMapper.readTree("{\"/endpoint\":{\"post\":{\"artifact\":\"\",\"engine\":\"fire\"}}}");
        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> koValidationService.validateDeploymentSpecification(deploymentSpec));
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
        assertEquals(KoValidationService.HAS_MISSING_ENDPOINT_IN_DEPLOYMENT_SPECIFICATION, activatorException.getMessage());
    }

    @Test
    public void validateDeployment_KoHasNoEndpointsDefinedInDeploymentSpec() throws JsonProcessingException {
        JsonNode deploymentSpec = objectMapper.readTree("{}");
        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> koValidationService.validateDeploymentSpecification(deploymentSpec));
        assertEquals(KoValidationService.HAS_NO_ENDPOINTS_DEFINED_IN_DEPLOYMENT_SPECIFICATION, activatorException.getMessage());
    }

    @Test
    public void validateWrapper_ValidKo() throws JsonProcessingException {
        JsonNode serviceSpec = objectMapper.readTree("{\"paths\":{\"/endpoint\":{\"post\":{\"stuff\":\"things\"}}}}");
        JsonNode deploymentSpec =
                objectMapper.readTree(
                        "{\"/endpoint\":{\"post\":{\"artifact\":\"Arty McFacts\",\"engine\":\"javascript\",\"function\":\"doorway\"}}}");

        KnowledgeObjectWrapper koWrapper = getWrapper(serviceSpec, deploymentSpec);
        koValidationService.validateKow(koWrapper);
    }

    @Test
    public void validateEndpoint_HasMissingPathInServiceSpec() throws JsonProcessingException {
        JsonNode serviceSpec = objectMapper.readTree("{\"paths\":{\"/WRONG\":{\"post\":{\"stuff\":\"things\"}}}}");
        JsonNode deploymentSpec =
                objectMapper.readTree(
                        "{\"/endpoint\":{\"post\":{\"artifact\":\"Arty McFacts\",\"engine\":\"javascript\",\"function\":\"doorway\"}}}");

        Endpoint endpoint = getEndpoint(serviceSpec, deploymentSpec);
        ActivatorException activatorException = Assert.assertThrows(ActivatorException.class,
                () -> koValidationService.validateEndpoint(endpoint));
        assertEquals(KoValidationService.HAS_MISSING_PATH_IN_SERVICE_SPECIFICATION, activatorException.getMessage());
    }

    private Endpoint getEndpoint(JsonNode serviceSpec, JsonNode deploymentSpec) {
        KnowledgeObjectWrapper wrapper = getWrapper(serviceSpec, deploymentSpec);
        return new Endpoint(wrapper, "endpoint");
    }

    private KnowledgeObjectWrapper getWrapper(JsonNode serviceSpec, JsonNode deploymentSpec) {
        JsonNode metadata = objectMapper.createObjectNode()
                .put("identifier", "ark:/naan/name/version")
                .put("@id","naan/name/version")
                .put("hasServiceSpecification", "value")
                .put("hasDeploymentSpecification", "value");
        KnowledgeObjectWrapper wrapper = new KnowledgeObjectWrapper(metadata);
        wrapper.addDeployment(deploymentSpec);
        wrapper.addService(serviceSpec);
        return wrapper;
    }

}

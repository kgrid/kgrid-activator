package org.kgrid.activator.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.domain.KoFields;

public class KoCreationTestHelper {
    private static final ObjectMapper mapper = new ObjectMapper();
    public static final String SERVICE_YAML_PATH = "service.yaml";
    public static final String DEPLOYMENT_YAML_PATH = "deployment.yaml";
    public static final String NAAN = "naan";
    public static final String NAME = "name";
    public static final String VERSION = "version";
    public static final String KO_PATH = NAAN + "-" + NAME + "-" + VERSION;
    public static final ArkId ARK_ID = new ArkId(NAAN, NAME, VERSION);
    public static final String ARTIFACT_PATH = "dist/main.js";
    public static final String ENGINE = "javascript";
    public static final String FUNCTION_NAME = "welcome";
    public static final String ENDPOINT_NAME = FUNCTION_NAME;
    public static final String POST_HTTP_METHOD = "post";
    public static final JsonNode ENDPOINT_POST_DEPLOYMENT_NODE = mapper.createObjectNode()
            .put("artifact", ARTIFACT_PATH)
            .put("engine", ENGINE)
            .put("function", FUNCTION_NAME);

    public static JsonNode generateMetadata(
            String serviceYamlPath,
            String deploymentYamlPath,
            boolean hasAtId,
            boolean hasIdentifier,
            boolean hasVersion,
            boolean hasType) {
        ObjectNode metadata = new ObjectMapper().createObjectNode();
        if (hasAtId) {
            metadata.put("@id", KO_PATH);
        }
        if (hasType) {
            metadata.put("@type", "koio:KnowledgeObject");
        }
        if (hasIdentifier) {
            metadata.put("identifier", ARK_ID.toString());
        }
        if (hasVersion) {
            metadata.put(KoFields.VERSION.asStr(), VERSION);
        }
        if (deploymentYamlPath != null) {
            metadata.put(KoFields.DEPLOYMENT_SPEC_TERM.asStr(), deploymentYamlPath);
        }
        if (serviceYamlPath != null) {
            metadata.put(KoFields.SERVICE_SPEC_TERM.asStr(), serviceYamlPath);
        }
        return metadata;
    }

    public static JsonNode generateMetadata() {
        return generateMetadata(SERVICE_YAML_PATH, DEPLOYMENT_YAML_PATH, true, true, true, true);
    }

    public static JsonNode getEndpointDeploymentJson() {
        return ENDPOINT_POST_DEPLOYMENT_NODE;
    }
}

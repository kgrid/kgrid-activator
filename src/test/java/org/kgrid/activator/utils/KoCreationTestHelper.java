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
    public static final String API_VERSION = "ApiVersion";
    public static final ArkId ARK_ID = new ArkId(NAAN, NAME, VERSION);
    public static final String ARTIFACT_PATH = "dist/main.js";
    public static final String JS_ENGINE = "javascript";
    public static final String NODE_ENGINE = "node";
    public static final String FUNCTION_NAME = "welcome";
    public static final String ENDPOINT_NAME = "endpoint";
    public static final String POST_HTTP_METHOD = "post";
    public static final JsonNode ENDPOINT_POST_DEPLOYMENT_NODE_JS = mapper.createObjectNode()
            .put("artifact", ARTIFACT_PATH)
            .put("engine", JS_ENGINE)
            .put("function", FUNCTION_NAME);
    public static final JsonNode ENDPOINT_POST_DEPLOYMENT_NODE_NODE = mapper.createObjectNode()
            .put("artifact", ARTIFACT_PATH)
            .put("engine", NODE_ENGINE)
            .put("function", FUNCTION_NAME);
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static JsonNode generateMetadata(
            String serviceYamlPath,
            String deploymentYamlPath,
            String naan,
            String name,
            String version,
            boolean hasAtId,
            boolean hasIdentifier,
            boolean hasVersion,
            boolean hasType) {
        ObjectNode metadata = objectMapper.createObjectNode();
        metadata.put("title", "Test Endpoint Title");
        if (hasAtId) {
            metadata.put("@id", String.format("%s/%s/%s", naan, name, version));
        }
        if (hasType) {
            metadata.put("@type", "koio:KnowledgeObject");
        }
        if (hasIdentifier) {
            metadata.put("identifier", new ArkId(naan, name, version).toString());
        }
        if (hasVersion) {
            metadata.put(KoFields.VERSION.asStr(), version);
        }
        if (deploymentYamlPath != null) {
            metadata.put(KoFields.DEPLOYMENT_SPEC_TERM.asStr(), deploymentYamlPath);
        }
        if (serviceYamlPath != null) {
            metadata.put(KoFields.SERVICE_SPEC_TERM.asStr(), serviceYamlPath);
        }
        return metadata;
    }

    public static JsonNode generateMetadata(String naan, String name, String version) {
        return generateMetadata(SERVICE_YAML_PATH, DEPLOYMENT_YAML_PATH, naan, name, version, true, true, true, true);
    }

    public static JsonNode getEndpointDeploymentJsonForEngine(String engine, String endpointName) {
        JsonNode deploymentNode;
        if (engine.equals("javascript")) {
            deploymentNode = ENDPOINT_POST_DEPLOYMENT_NODE_JS;
        } else {
            deploymentNode = ENDPOINT_POST_DEPLOYMENT_NODE_NODE;
        }
        return mapper.createObjectNode().set("/" + endpointName, mapper.createObjectNode().set(POST_HTTP_METHOD, deploymentNode));
    }

    public static JsonNode generateServiceNode() {
        ObjectNode serviceNode = objectMapper.createObjectNode();
        serviceNode.set("info", objectMapper.createObjectNode().put("version", API_VERSION));
        serviceNode.set("paths", objectMapper.createObjectNode().set("/welcome", objectMapper.createObjectNode()
                .set("post", objectMapper.createObjectNode().set("requestBody", objectMapper.createObjectNode()
                        .set("content", objectMapper.createObjectNode()
                                .set("application/json", objectMapper.createObjectNode()))))));
        return serviceNode;
    }
}

package org.kgrid.activator.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.kgrid.activator.services.Endpoint;
import org.kgrid.shelf.domain.ArkId;
import org.kgrid.shelf.domain.KnowledgeObjectWrapper;
import org.kgrid.shelf.domain.KoFields;
import org.springframework.http.MediaType;

import java.net.URI;

public class KoCreationTestHelper {
    private static final ObjectMapper mapper = new ObjectMapper();
    public static final String SERVICE_YAML_PATH = "service.yaml";
    public static final String DEPLOYMENT_YAML_PATH = "deployment.yaml";
    public static final String JS_NAAN = "naan";
    public static final String JS_NAME = "name";
    public static final String JS_VERSION = "version";
    public static final String JS_API_VERSION = "jsApiVersion";
    public static final String JS_ENDPOINT_NAME = "endpoint";
    public static final String JS_ENDPOINT_ID = String.format("%s/%s/%s/%s", JS_NAAN, JS_NAME, JS_API_VERSION, JS_ENDPOINT_NAME);
    public static final URI JS_ENDPOINT_URI = URI.create(JS_ENDPOINT_ID);
    public static final ArkId JS_ARK_ID = new ArkId(JS_NAAN, JS_NAME, JS_VERSION);
    public static final String JS_ENGINE = "javascript";

    public static final String NODE_NAAN = "node-naan";
    public static final String NODE_NAME = "node-name";
    public static final String NODE_VERSION = "node-version";
    public static final String NODE_ENDPOINT_NAME = "node-endpoint";
    public static final String NODE_API_VERSION = "nodeApiVersion";
    public static final String NODE_ENDPOINT_ID = String.format("%s/%s/%s/%s", NODE_NAAN, NODE_NAME, NODE_API_VERSION, NODE_ENDPOINT_NAME);
    public static final URI NODE_ENDPOINT_URI = URI.create(JS_ENDPOINT_ID);
    public static final ArkId NODE_ARK_ID = new ArkId(NODE_NAAN, NODE_NAME, NODE_VERSION);

    public static final String NODE_ENGINE = "node";
    public static final String ARTIFACT_PATH = "dist/main.js";
    public static final String FUNCTION_NAME = "welcome";
    public static final String POST_HTTP_METHOD = "post";
    public static MediaType CONTENT_TYPE = MediaType.APPLICATION_JSON;
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

    public static JsonNode generateServiceNode(String engine) {
        ObjectNode serviceNode;
        if (engine.equals(JS_ENGINE)) {
            serviceNode = objectMapper.createObjectNode();
            serviceNode.set("info", objectMapper.createObjectNode().put("version", JS_API_VERSION));
            serviceNode.set("paths", objectMapper.createObjectNode().set("/" + JS_ENDPOINT_NAME, objectMapper.createObjectNode()
                    .set("post", objectMapper.createObjectNode().set("requestBody", objectMapper.createObjectNode()
                            .set("content", objectMapper.createObjectNode()
                                    .set(CONTENT_TYPE.toString(), objectMapper.createObjectNode()))))));

        } else if (engine.equals(NODE_ENGINE)) {
            serviceNode = objectMapper.createObjectNode();
            serviceNode.set("info", objectMapper.createObjectNode().put("version", NODE_API_VERSION));
            serviceNode.set("paths", objectMapper.createObjectNode().set("/" + NODE_ENDPOINT_NAME, objectMapper.createObjectNode()
                    .set("post", objectMapper.createObjectNode().set("requestBody", objectMapper.createObjectNode()
                            .set("content", objectMapper.createObjectNode()
                                    .set(CONTENT_TYPE.toString(), objectMapper.createObjectNode()))))));

        } else {
            return null;
        }

        return serviceNode;
    }

    public static Endpoint getEndpointForEngine(String engine) {
        if (engine.equals(NODE_ENGINE)) {
            KnowledgeObjectWrapper nodeKow = new KnowledgeObjectWrapper(generateMetadata(NODE_NAAN, NODE_NAME, NODE_VERSION));
            nodeKow.addService(generateServiceNode(engine));
            nodeKow.addDeployment(getEndpointDeploymentJsonForEngine(engine, NODE_ENDPOINT_NAME));
            return new Endpoint(nodeKow, NODE_ENDPOINT_NAME);
        } else if (engine.equals(JS_ENGINE)) {
            KnowledgeObjectWrapper nodeKow = new KnowledgeObjectWrapper(generateMetadata(JS_NAAN, JS_NAME, JS_VERSION));
            nodeKow.addService(generateServiceNode(engine));
            nodeKow.addDeployment(getEndpointDeploymentJsonForEngine(engine, JS_ENDPOINT_NAME));
            return new Endpoint(nodeKow, JS_ENDPOINT_NAME);
        } else {
            return null;
        }
    }
}

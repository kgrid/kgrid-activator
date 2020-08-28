package org.kgrid.activator.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.kgrid.shelf.domain.ArkId;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;

public class RepoUtils {

    public static final ArkId A_B_C = new ArkId("a", "b", "c");
    public static final ArkId C_D_E = new ArkId("c", "d", "e");
    public static final ArkId C_D_F = new ArkId("c", "d", "f");
    public static final ArkId TEST_SERVICE_EXTENSION_ONLY = new ArkId("test", "service", "extensiononly");
    static ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

    public static JsonNode getYamlTestFile(ArkId ark, String filePath) throws IOException {
        Resource r = resourceResolver.getResource("/shelf/" + ark.getFullDashArk() + "/" + filePath);
        return new YAMLMapper().readTree(r.getFile());
    }

    public static JsonNode getJsonTestFile(ArkId ark, String filePath) throws IOException {
        Resource r = resourceResolver.getResource("/shelf/" + ark.getFullDashArk() + "/" + filePath);
        return new ObjectMapper().readTree(r.getFile());
    }
}

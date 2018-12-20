package org.kgrid.activator.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.nio.file.Files;
import org.kgrid.shelf.domain.ArkId;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

public class RepoUtils {

  public static final ArkId A_B = new ArkId("a-b");
  public static final ArkId C_D = new ArkId("c-d");
  public static final ArkId A_B_C = new ArkId("a-b/c");
  public static final ArkId C_D_E = new ArkId("c-d/e");
  public static final ArkId C_D_F = new ArkId("c-d/f");
  static ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
  static ObjectMapper yamlMapper = new YAMLMapper();
  static ObjectMapper jsonMapper = new ObjectMapper();

  /*
   ** Loaders for the mock repo
   */
  public static JsonNode getYamlTestFile(String ark, String filePath) throws IOException {

    Resource r = resourceResolver.getResource("/shelf/" + ark + "/" + filePath);

    final JsonNode sd = yamlMapper.readTree(r.getFile());
    return sd;
  }

  public static JsonNode getJsonTestFile(String ark, String filePath) throws IOException {

    Resource r = resourceResolver.getResource("/shelf/" + ark + "/" + filePath);

    final JsonNode sd = jsonMapper.readTree(r.getFile());
    return sd;
  }

  public static byte[] getBinaryTestFile(String ark, String filePath) throws IOException {

    Resource r = resourceResolver.getResource("/shelf/" + ark + "/" + filePath);

    byte[] binary = Files.readAllBytes(r.getFile().toPath());

    return binary;
  }



}

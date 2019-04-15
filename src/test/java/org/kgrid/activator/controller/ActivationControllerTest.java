package org.kgrid.activator.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kgrid.activator.KgridActivatorApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = KgridActivatorApplication.class)
public class ActivationControllerTest {

  @Autowired
  private WebApplicationContext webApplicationContext;
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper mapper;
  private RestTemplate restTemplate;


  @Before
  public void setUp(){
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    restTemplate = new RestTemplate();
  }

  @Test
  public void endpointInvocationReturnsResult() throws Exception {
    MvcResult result = getResultActions("/c/d/f/welcome", "{\"name\" : \"tester\"}")
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andReturn();

    JsonNode content = mapper
        .readTree(result.getResponse().getContentAsByteArray());

    assertEquals("Welcome to Knowledge Grid, tester", content.get("result").asText());

  }
  private ResultActions getResultActions(String endpointPath, String content) throws Exception {
    return mockMvc.perform(
        post(endpointPath).content(content)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8));
  }

  @Test
  public void endpointNoRequestBodyError() throws Exception {
    MvcResult result = getResultActions("/c/d/f/welcome", "")
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andReturn();

    JsonNode content = mapper
        .readTree(result.getResponse().getContentAsByteArray());

    assertTrue(content.get("Detail").asText().startsWith("Required request body is missing"));
  }

  @Test
  public void endpointBlankServiceError() throws Exception {
    MvcResult result = getResultActions("/bad/ko/blankservice/welcome", "{\"name\":\"tester\"}")
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andReturn();

    JsonNode content = mapper
        .readTree(result.getResponse().getContentAsByteArray());

    assertEquals("No endpoint found for bad-ko/blankservice/welcome", content.get("Detail").asText());
  }

  @Test
  public void endpointFunctionMismatchError() throws Exception {
    MvcResult result = getResultActions("/bad/ko/functionmismatch/welcome", "{\"name\":\"tester\"}")
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andReturn();

    JsonNode content = mapper
        .readTree(result.getResponse().getContentAsByteArray());

    assertEquals("No endpoint found for bad-ko/functionmismatch/welcome", content.get("Detail").asText());
  }

  @Test
  public void endpointNoMetadataError() throws Exception {
    MvcResult result = getResultActions("/bad/ko/nometadata/welcome", "{\"name\":\"tester\"}")
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andReturn();

    JsonNode content = mapper
        .readTree(result.getResponse().getContentAsByteArray());

    assertEquals("No endpoint found for bad-ko/nometadata/welcome", content.get("Detail").asText());
  }

  @Test
  public void endpointNoServiceError() throws Exception {
    MvcResult result = getResultActions("/bad/ko/noservice/welcome", "{\"name\":\"tester\"}")
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andReturn();

    JsonNode content = mapper
        .readTree(result.getResponse().getContentAsByteArray());

    assertEquals("No endpoint found for bad-ko/noservice/welcome", content.get("Detail").asText());
  }

  @Test
  public void onlyMetadata() throws Exception {
    MvcResult result = getResultActions("/bad/ko/onlymetadata/welcome", "{\"name\":\"tester\"}")
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andReturn();

    JsonNode content = mapper
        .readTree(result.getResponse().getContentAsByteArray());

    assertEquals("No endpoint found for bad-ko/onlymetadata/welcome", content.get("Detail").asText());
  }

}

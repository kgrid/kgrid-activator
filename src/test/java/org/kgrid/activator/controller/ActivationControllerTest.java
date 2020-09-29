package org.kgrid.activator.controller;

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
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = KgridActivatorApplication.class)
public class ActivationControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void endpointInvocationReturnsResult() throws Exception {
        MvcResult result =
                getResultActions("/c/d/welcome?v=f", "{\"name\" : \"tester\"}")
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andReturn();

        JsonNode content = mapper.readTree(result.getResponse().getContentAsByteArray());

        assertEquals("Welcome to Knowledge Grid, tester", content.get("result").asText());
    }

    private ResultActions getResultActions(String endpointPath, String content) throws Exception {
        return mockMvc.perform(
                post(endpointPath)
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
    }

    @Test
    public void endpointNoRequestBodyError() throws Exception {
        MvcResult result =
                getResultActions("/c/d/welcome?v=f", "")
                        .andExpect(status().isInternalServerError())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andReturn();

        JsonNode content = mapper.readTree(result.getResponse().getContentAsByteArray());

        assertTrue(content.get("Detail").asText().startsWith("Required request body is missing"));
    }

    @Test
    public void endpointBlankServiceError() throws Exception {
        MvcResult result =
                getResultActions("/bad/koio/welcome?v=blankservice", "{\"name\":\"tester\"}")
                        .andExpect(status().isInternalServerError())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andReturn();

        JsonNode content = mapper.readTree(result.getResponse().getContentAsByteArray());

        assertEquals(
                "No endpoint found for bad/koio/blankservice/welcome", content.get("Detail").asText());
    }

    @Test
    public void serviceSpecFunctionMismatchError() throws Exception {
        MvcResult result =
                getResultActions("/bad/koio/welcome?v=servicespecmismatch", "{\"name\":\"tester\"}")
                        .andExpect(status().isInternalServerError())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andReturn();

        JsonNode content = mapper.readTree(result.getResponse().getContentAsByteArray());

        assertEquals(
                "Exception for endpoint bad/koio/servicespecmismatch/welcome Code execution error", content.get("Detail").asText());
    }

    @Test
    public void endpointNoMetadataError() throws Exception {
        MvcResult result =
                getResultActions("/bad/koio/welcome?v=nometadata", "{\"name\":\"tester\"}")
                        .andExpect(status().isInternalServerError())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andReturn();

        JsonNode content = mapper.readTree(result.getResponse().getContentAsByteArray());

        assertEquals(
                "No endpoint found for bad/koio/nometadata/welcome", content.get("Detail").asText());
    }

    @Test
    public void endpointNoServiceError() throws Exception {
        MvcResult result =
                getResultActions("/bad/koio/welcome?v=noservice", "{\"name\":\"tester\"}")
                        .andExpect(status().isInternalServerError())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andReturn();

        JsonNode content = mapper.readTree(result.getResponse().getContentAsByteArray());

        assertEquals(
                "No endpoint found for bad/koio/noservice/welcome", content.get("Detail").asText());
    }

    @Test
    public void endpointOnlyMetadataError() throws Exception {
        MvcResult result =
                getResultActions("/bad/koio/welcome?v=onlymetadata", "{\"name\":\"tester\"}")
                        .andExpect(status().isInternalServerError())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andReturn();

        JsonNode content = mapper.readTree(result.getResponse().getContentAsByteArray());

        assertEquals(
                "No endpoint found for bad/koio/onlymetadata/welcome", content.get("Detail").asText());
    }
}

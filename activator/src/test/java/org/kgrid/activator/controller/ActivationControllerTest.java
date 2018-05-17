package org.kgrid.activator.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umich.lhs.activator.KgridActivatorApplication;
import edu.umich.lhs.activator.TestUtils;
import java.util.LinkedHashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
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

  @Test
  public void findAllLoadedKnowledgeObjectEndPoints() throws Exception {

    ResultActions result = mockMvc.perform(get("/"));

    result.andExpect(status().isOk())
        .andExpect(content().contentType(TestUtils.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.*", hasSize(2)));
  }

  @Before
  public void setUp(){
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    restTemplate = new RestTemplate();
  }

  @Test
  public void processKnowledgeObjectEndPoint() throws Exception {


     ResultActions result = mockMvc.perform(
        post("/99999/newko/v0.0.1/welcome").content("{\"name\" : \"tester\"}")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8));

    result.andExpect(status().isOk())
        .andExpect(content().contentType(TestUtils.APPLICATION_JSON_UTF8));

    System.out.print( result );
  }

}
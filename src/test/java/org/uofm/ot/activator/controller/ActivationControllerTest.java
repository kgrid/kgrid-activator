package org.uofm.ot.activator.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.uofm.ot.activator.repository.RemoteShelf;
import org.uofm.ot.activator.services.ActivationService;
import org.uofm.ot.activator.domain.ArkId;
import org.uofm.ot.activator.domain.Result;

/**
 * Created by nggittle on 3/31/17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class ActivationControllerTest {

  private MockMvc mockMvc;

  @Mock
  private ActivationService activationService;

  @InjectMocks
  private ActivationController exCon;

  @Mock
  private RemoteShelf remoteShelf;

  @Before
  public void setUpMocks() {
    mockMvc = MockMvcBuilders
        .standaloneSetup(exCon)
        .build();
  }

  @Test
  public void testGetResultByArkIdSuccess() throws Exception {

    String naan = "hello";
    String name = "world";
    ArkId arkId = new ArkId(naan, name);

    Result result = new Result("success");

    HashMap<String, Object> inputs = new HashMap<>();
    inputs.put("Test", "1");

    Mockito.when(activationService.getResultByArkId(inputs, arkId)).thenReturn(result);
    Mockito.when(remoteShelf.getAbsoluteObjectUrl(arkId)).thenReturn("testURL/ark:/hello/world");

    mockMvc.perform(post("/knowledgeObject/ark:/{naan}/{name}/result", naan, name)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .content("{\"Test\":\"1\"}"))
        .andExpect(status().isOk())
        .andExpect(content().string("{\"result\":\"success\",\"source\":null,\"metadata\":null}"));
  }

}
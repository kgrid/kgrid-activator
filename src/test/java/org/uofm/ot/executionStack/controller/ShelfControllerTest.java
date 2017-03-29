package org.uofm.ot.executionStack.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.uofm.ot.executionStack.ObjectTellerExecutionStackApplication;
import org.uofm.ot.executionStack.TestUtils;
import org.uofm.ot.executionStack.reposity.Shelf;
import org.uofm.ot.executionStack.transferObjects.ArkId;
import org.uofm.ot.executionStack.transferObjects.KnowledgeObjectBuilder;
import org.uofm.ot.executionStack.transferObjects.KnowledgeObjectDTO;

/**
 * Created by pboisver on 1/16/17.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ObjectTellerExecutionStackApplication.class})
@WebAppConfiguration
public class ShelfControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private Shelf shelf;

    @Value("${stack.shelf.path:.}")
    private String localStoragePath;

    @Value("${stack.shelf.name:shelf}")
    private String shelfName;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        File folderPath = new File(localStoragePath, shelfName);

        //Clear the shelf
        if(folderPath.exists() != false){
            String[] itemsOnShelf = folderPath.list();
            for(String file : itemsOnShelf) {
                File currentFile = new File(folderPath.getPath(), file);
                currentFile.delete();
            }
        }
    }

    @Test
    public void getObjectFromLocalShelf() throws Exception {

        KnowledgeObjectDTO ko = new KnowledgeObjectBuilder().build();
        ArkId arkId = new ArkId("99999", "fk4df70k9j");

        KnowledgeObjectDTO ko2 = new KnowledgeObjectBuilder().build();
        ArkId arkId2 = new ArkId("99999", "fk4df70k9k");

        shelf.saveObject(ko, arkId);
        //shelf.saveObject(ko2, arkId2);

        ResultActions result = mockMvc.perform(get("/shelf"));
        result
            .andExpect(status().isOk())
            .andExpect(content().contentType(TestUtils.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.*", hasSize(1)))
            .andExpect(jsonPath("$.[0].ArkId", is("ark:/99999/fk4df70k9j")));
    }


    @Test
    public void addBlankKOtoShelf() throws Exception {
        KnowledgeObjectDTO ko = new KnowledgeObjectBuilder().build();

        mockMvc.perform(put("/shelf/ark:/{naan}/{name}","99999", "fk4df70k9j")
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(ko))
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(TestUtils.APPLICATION_TEXT_UTF8))
            .andExpect(content().string("Object Added on the shelf"));
    }

    @Test
    public void deleteObjectFromShelf() throws Exception {
        KnowledgeObjectDTO ko = new KnowledgeObjectBuilder().build();
        ArkId arkId = new ArkId("99999", "fk4df70k9j");

        shelf.saveObject(ko, arkId);

        mockMvc.perform(delete("/shelf/ark:/{naan}/{name}","99999", "fk4df70k9j"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(TestUtils.APPLICATION_TEXT_UTF8))
            .andExpect(content().string( "Object with ArkId " + arkId + " no longer on the Shelf"));
    }

    @Test
    public void insertBlankObjectOntoShelfAndRetrievePayload() throws Exception {
        KnowledgeObjectDTO ko = new KnowledgeObjectBuilder().build();
        ArkId arkId = new ArkId("99999", "fk4df70k9j");

        shelf.saveObject(ko, arkId);

        mockMvc.perform(get("/knowledgeObject/ark:/{naan}/{name}/payload/content", "99999", "fk4df70k9j"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void insertPayloadObjectOntoShelfAndRetrievePayload() throws Exception {
        KnowledgeObjectDTO ko = new KnowledgeObjectBuilder()
            .payloadContent("payload")
            .build();
        ArkId arkId = new ArkId("99999", "fk4df70k9j");

        shelf.saveObject(ko, arkId);

        mockMvc.perform(get("/knowledgeObject/ark:/{naan}/{name}/payload/content", "99999", "fk4df70k9j"))
            .andExpect(status().isOk())
            .andExpect(content().string("payload"));
    }


    @Test
    public void checkWhereami() throws Exception {

        mockMvc.perform(get("/whereami"))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"objTellerInterface.where()\":\"http://localhost\"}"));

    }

    @Test
    public void test404() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isNotFound());

    }

}
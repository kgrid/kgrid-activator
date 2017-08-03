package edu.umich.lhs.activator.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.umich.lhs.activator.KgridActivatorApplication;
import edu.umich.lhs.activator.TestUtils;
import edu.umich.lhs.activator.domain.ArkId;
import edu.umich.lhs.activator.domain.KnowledgeObject;
import edu.umich.lhs.activator.repository.Shelf;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import edu.umich.lhs.activator.domain.KnowledgeObjectBuilder;

/**
 * Created by pboisver on 1/16/17.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {KgridActivatorApplication.class})
@WebAppConfiguration
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class ShelfControllerTest {

    private MockMvc mockMvc;

    private static final String SHELF_PATH = "stack.shelf.path";

    @Autowired
    private Shelf shelf;

    @Value("${stack.shelf.name:shelf}")
    private String shelfName;

    @Autowired
    private WebApplicationContext webApplicationContext;

    // Set the shelf path property
    @BeforeClass
    public static void initializePath() throws IOException {
        String tempFilepath;
        tempFilepath = Files.createTempDirectory("shelf").toString();
        System.setProperty(SHELF_PATH, (tempFilepath != null ? tempFilepath : "tempShelf"));
    }

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @After
    public void tearDown() {
        File folderPath = new File(System.getProperty(SHELF_PATH), shelfName);

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

        KnowledgeObject ko = new KnowledgeObjectBuilder().build();
        ArkId arkId = new ArkId("99999", "fk4df70k9j");

        shelf.saveObject(ko, arkId);

        ResultActions result = mockMvc.perform(get("/shelf"));
        result
            .andExpect(status().isOk())
            .andExpect(content().contentType(TestUtils.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.*", hasSize(3)))
            .andExpect(jsonPath("$.[0].metadata.arkId.arkId", is("ark:/99999/fk4df70k9j")));
    }

    @Test
    public void addBlankKOtoShelf() throws Exception {
        KnowledgeObject ko = new KnowledgeObjectBuilder().build();

        mockMvc.perform(put("/shelf/ark:/{naan}/{name}","99999", "fk4df70k9j")
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(ko))
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(TestUtils.APPLICATION_TEXT_UTF8))
            .andExpect(content().string("Object ark:/99999/fk4df70k9j added to the shelf"));
    }

    @Test
    public void deleteObjectFromShelf() throws Exception {
        KnowledgeObject ko = new KnowledgeObjectBuilder().build();
        ArkId arkId = new ArkId("99999", "fk4df70k9j");

        shelf.saveObject(ko, arkId);

        mockMvc.perform(delete("/shelf/ark:/{naan}/{name}","99999", "fk4df70k9j"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(TestUtils.APPLICATION_TEXT_UTF8))
            .andExpect(content().string(containsString(arkId.getArkId())))
            .andExpect(content().string(containsString("no longer on the user shelf")));
    }

    @Test
    public void insertBlankObjectOntoShelfAndRetrievePayload() throws Exception {
        KnowledgeObject ko = new KnowledgeObjectBuilder().build();
        ArkId arkId = new ArkId("99999", "fk4df70k9j");

        shelf.saveObject(ko, arkId);

        mockMvc.perform(get("/knowledgeObject/ark:/{naan}/{name}/payload/content", "99999", "fk4df70k9j"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void insertPayloadObjectOntoShelfAndRetrievePayload() throws Exception {
        KnowledgeObject ko = new KnowledgeObjectBuilder()
            .payloadContent("payload")
            .build();
        ArkId arkId = new ArkId("99999", "fk4df70k9j");

        shelf.saveObject(ko, arkId);

        mockMvc.perform(get("/knowledgeObject/ark:/{naan}/{name}/payload/content", "99999", "fk4df70k9j"))
            .andExpect(status().isOk())
            .andExpect(content().string("payload"));
    }

    @Test
    public void test404() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isNotFound());

    }


}
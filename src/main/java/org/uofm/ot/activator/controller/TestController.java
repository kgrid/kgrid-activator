package org.uofm.ot.activator.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.uofm.ot.activator.domain.KnowledgeObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by pboisver on 4/7/17.
 */

@RestController
@RequestMapping("test")
public class TestController {

    @Autowired
    com.mashape.unirest.http.ObjectMapper unirestObjectMapper;

    public static final String LIBRARY_URL_HTTP = "http://dlhs-fedora-dev.med.umich.edu/ObjectTeller/knowledgeObject/ark:/99999/fk4th8sn52";
//    public static final String LIBRARY_URL_HTTP = "http://github.com";


    @GetMapping("1")
    Map<String, String> getResults() {

        Map<String, String> results = new HashMap<>();

        results.put("first", "1");
        results.put("second", "2");

        return results;
    }

    @GetMapping(path = "2", produces = MediaType.APPLICATION_JSON_VALUE)
    KnowledgeObject getRemoteResult() {

        HttpClient instance = HttpClientBuilder.create()
                .setRedirectStrategy(new LaxRedirectStrategy()).build();

        RestTemplate rest = new RestTemplate(new HttpComponentsClientHttpRequestFactory(instance));

        ResponseEntity<KnowledgeObject> response = rest.getForEntity(LIBRARY_URL_HTTP, KnowledgeObject.class);

        return response.getBody();
    }

    @GetMapping(path = "3", produces = MediaType.APPLICATION_JSON_VALUE)
    KnowledgeObject getRemoteResultWithUnirest()  {

        Unirest.setObjectMapper(unirestObjectMapper);


        HttpResponse<KnowledgeObject> response = null;
        try {
            response = Unirest.get(LIBRARY_URL_HTTP).asObject(KnowledgeObject.class);
        } catch (UnirestException e) {
            e.printStackTrace();
        }

//        HttpResponse<String> response = Unirest.get(LIBRARY_URL_HTTP).asString();
//
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//
//        KnowledgeObject ko = mapper.readValue(response.getBody(), KnowledgeObject.class);

        return response.getBody();
    }

    @Bean
     com.mashape.unirest.http.ObjectMapper getObjectMapper() {
        return new com.mashape.unirest.http.ObjectMapper() {
                private ObjectMapper jacksonObjectMapper
                        = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                public <T> T readValue(String value, Class<T> valueType) {
                    try {
                        return jacksonObjectMapper.readValue(value, valueType);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                public String writeValue(Object value) {
                    try {
                        return jacksonObjectMapper.writeValueAsString(value);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
    }


}

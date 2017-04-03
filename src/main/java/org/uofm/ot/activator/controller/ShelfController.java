package org.uofm.ot.activator.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.uofm.ot.activator.exception.OTExecutionStackException;
import org.uofm.ot.activator.repository.RemoteShelf;
import org.uofm.ot.activator.repository.Shelf;
import org.uofm.ot.activator.transferObjects.ArkId;
import org.uofm.ot.activator.transferObjects.KnowledgeObjectDTO;
import org.uofm.ot.activator.transferObjects.KnowledgeObjectDTO.Payload;


@RestController
@CrossOrigin
public class ShelfController {

    private static final String NAME_TO_THING_ADD = "http://n2t.net/";
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private RemoteShelf objTellerInterface;
    @Autowired
    private Shelf localStorage;

    @PutMapping(consumes = {MediaType.TEXT_PLAIN_VALUE},
        path = {"/knowledgeObject/ark:/{naan}/{name}", "/shelf/ark:/{naan}/{name}","/ko/{naan}-{name}"})
    public ResponseEntity<String> checkOutObjectByArkId(ArkId arkId) throws OTExecutionStackException {
        ResponseEntity<String> result = null;

        KnowledgeObjectDTO dto = objTellerInterface.checkOutByArkId(arkId);
        localStorage.saveObject(dto, arkId);


        result = new ResponseEntity<String>("Object Added on the shelf", HttpStatus.OK);

        return result;

    }


    @PutMapping(consumes = {MediaType.APPLICATION_JSON_VALUE},
            path = {"/knowledgeObject/ark:/{naan}/{name}", "/shelf/ark:/{naan}/{name}"})
    public ResponseEntity<String> checkOutObject(ArkId arkId, @RequestBody KnowledgeObjectDTO dto) throws OTExecutionStackException {
        try {
            dto.url = NAME_TO_THING_ADD + arkId;
            localStorage.saveObject(dto, arkId);
            ResponseEntity<String> result = new ResponseEntity<String>("Object Added on the shelf", HttpStatus.OK);
            return result;
        } catch (Exception e) {
            throw new OTExecutionStackException("Unable to save object with ArkId: " + arkId + ", root cause: " + e.getMessage(), e);
        }
    }

    @GetMapping(path = {"/knowledgeObject", "/shelf"})
    public List<Map<String, String>> retrieveObjectsOnShelf() {
        return localStorage.getAllObjects();
    }


    @DeleteMapping(path = {"/shelf/ark:/{naan}/{name}", "/knowledgeObject/ark:/{naan}/{name}"})
    public ResponseEntity<String> deleteObjectOnTheShelfByArkId(ArkId arkId) {
        localStorage.deleteObject(arkId);
        return new ResponseEntity<String>("Object with ArkId " + arkId + " no longer on the Shelf", HttpStatus.OK);
    }

    @GetMapping(path = {"/knowledgeObject/ark:/{naan}/{name}/payload/content", "/shelf/ark:/{naan}/{name}/payload/content"})
    public String retrievePayloadContent(ArkId arkId) {

        Payload payload = retrieveObjectPayload(arkId);
        if (payload.content == null) {
            throw new OTExecutionStackException("Content is null for object with Ark Id:  " + arkId);
        }
        return payload.content;

    }

    @GetMapping(path = {"/knowledgeObject/ark:/{naan}/{name}/payload", "/shelf/ark:/{naan}/{name}/payload"})
    public Payload retrieveObjectPayload(ArkId arkId) {

        KnowledgeObjectDTO dto = retrieveObjectOnShelf(arkId);
        if (dto.payload == null) {
            throw new OTExecutionStackException("Payload is null for object with Ark Id:  " + arkId);
        }
        return dto.payload;
    }

    @GetMapping(path = {"/knowledgeObject/ark:/{naan}/{name}", "/shelf/ark:/{naan}/{name}"})
    public KnowledgeObjectDTO retrieveObjectOnShelf(ArkId arkId) {
        return localStorage.getObject(arkId);
    }



    @GetMapping("/whereami")
    public Map<String, String> where(HttpServletRequest req) {

        Map<String, String> here = new HashMap<>();

//		here.put("req.getRequestURL()",req.getRequestURL().toString());

        here.put("objTellerInterface.where()", objTellerInterface.getLibraryPath());

        return here;
    }
}

package edu.umich.lhs.activator.controller;

import edu.umich.lhs.activator.domain.KnowledgeObject;
import edu.umich.lhs.activator.domain.KnowledgeObject.Payload;
import edu.umich.lhs.activator.exception.ActivatorException;
import edu.umich.lhs.activator.repository.RemoteShelf;
import edu.umich.lhs.activator.repository.Shelf;
import edu.umich.lhs.activator.repository.Shelf.SourcedKO;
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
import edu.umich.lhs.activator.domain.ArkId;


@RestController
@CrossOrigin
public class ShelfController {

    private static final String NAME_TO_THING_ADD = "http://n2t.net/";
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private RemoteShelf libraryInterface;
    @Autowired
    private Shelf localStorage;

    @PutMapping(path = {"/knowledgeObject/ark:/{naan}/{name}", "/shelf/ark:/{naan}/{name}","/ko/{naan}-{name}"})
    public ResponseEntity<String> checkOutObjectByArkId(ArkId arkId) throws ActivatorException {
        ResponseEntity<String> result;
        String response = "Object " + arkId.getArkId() + " added to the shelf";

        KnowledgeObject dto = libraryInterface.checkOutByArkId(arkId);
        if(localStorage.isBuiltinObject(arkId)) {
            response = "Object " + arkId.getArkId() + " added to the shelf, overriding existing built-in object.";
        }
        localStorage.saveObject(dto, arkId);

        result = new ResponseEntity<>(response, HttpStatus.OK);

        return result;

    }


    @PutMapping(consumes = {MediaType.APPLICATION_JSON_VALUE},
            path = {"/knowledgeObject/ark:/{naan}/{name}", "/shelf/ark:/{naan}/{name}"})
    public ResponseEntity<String> checkOutObject(ArkId arkId, @RequestBody KnowledgeObject dto) throws ActivatorException {
        try {
            String response = "Object " + arkId.getArkId() + " added to the shelf";
            if(localStorage.isBuiltinObject(arkId)) {
                response = "Object " + arkId.getArkId() + " added to the shelf, overriding existing built-in object.";
            }
            dto.url = NAME_TO_THING_ADD + arkId;
            localStorage.saveObject(dto, arkId);
            ResponseEntity<String> result = new ResponseEntity<String>(response, HttpStatus.OK);
            return result;
        } catch (Exception e) {
            throw new ActivatorException("Unable to save object with ArkId: " + arkId + ", root cause: " + e.getMessage(), e);
        }
    }

    @GetMapping(path = {"/knowledgeObject", "/shelf"})
    public List<SourcedKO> retrieveObjectsOnShelf() {
        return localStorage.getAllObjects();
    }


    @DeleteMapping(path = {"/shelf/ark:/{naan}/{name}", "/knowledgeObject/ark:/{naan}/{name}"})
    public ResponseEntity<String> deleteObjectOnTheShelfByArkId(ArkId arkId) {
        String response = "Object with ArkId " + arkId + " no longer on the user shelf";

        localStorage.deleteObject(arkId);
        if(localStorage.isBuiltinObject(arkId)) {
            response = response + " built-in object now exposed.";
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(path = {"/knowledgeObject/ark:/{naan}/{name}/payload/content", "/shelf/ark:/{naan}/{name}/payload/content"})
    public String retrievePayloadContent(ArkId arkId) {

        Payload payload = retrieveObjectPayload(arkId);
        if (payload.getContent() == null) {
            throw new ActivatorException("Content is null for object with Ark Id:  " + arkId);
        }
        return payload.getContent();

    }

    @GetMapping(path = {"/knowledgeObject/ark:/{naan}/{name}/payload", "/shelf/ark:/{naan}/{name}/payload"})
    public Payload retrieveObjectPayload(ArkId arkId) {

        KnowledgeObject dto = retrieveObjectOnShelf(arkId);
        if (dto.payload == null) {
            throw new ActivatorException("Payload is null for object with Ark Id:  " + arkId);
        }
        return dto.payload;
    }

    @GetMapping(path = {"/knowledgeObject/ark:/{naan}/{name}", "/shelf/ark:/{naan}/{name}"})
    public KnowledgeObject retrieveObjectOnShelf(ArkId arkId) {
        return localStorage.getObject(arkId);
    }



    @GetMapping("/whereami")
    public Map<String, String> where(HttpServletRequest req) {

        Map<String, String> here = new HashMap<>();

//		here.put("req.getRequestURL()",req.getRequestURL().toString());

        here.put("Library Path", libraryInterface.getLibraryPath());

        return here;
    }
}

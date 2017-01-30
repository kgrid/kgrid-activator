package org.uofm.ot.executionStack.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uofm.ot.executionStack.adapter.PythonAdapter;
import org.uofm.ot.executionStack.exception.OTExecutionStackEntityNotFoundException;
import org.uofm.ot.executionStack.exception.OTExecutionStackException;
import org.uofm.ot.executionStack.objectTellerLayer.ObjectTellerInterface;
import org.uofm.ot.executionStack.reposity.Shelf;
import org.uofm.ot.executionStack.transferObjects.*;
import org.uofm.ot.executionStack.transferObjects.KnowledgeObjectDTO.Payload;
import org.uofm.ot.executionStack.util.CodeMetadataConvertor;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@CrossOrigin
public class ExecutionStackController {

    private static final String NAME_TO_THING_ADD = "http://n2t.net/";
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ObjectTellerInterface objTellerInterface;
    @Autowired
    private Shelf localStorage;
    @Autowired
    private CodeMetadataConvertor convertor;
    @Autowired
    private PythonAdapter adapter;

    @PutMapping(path = {"/knowledgeObject/ark:/{naan}/{name}", "/shelf/ark:/{naan}/{name}","/ko/{naan}-{name}"})
    public ResponseEntity<String> checkOutObjectByArkId(ArkId arkId) throws OTExecutionStackException {
        ResponseEntity<String> result = null;

        KnowledgeObjectDTO dto = objTellerInterface.checkOutByArkId(arkId);
        localStorage.saveObject(dto, arkId);


        result = new ResponseEntity<String>("Object Added on the shelf", HttpStatus.OK);

        return result;

    }


    @PutMapping(consumes = {MediaType.APPLICATION_JSON_VALUE},
            value = {"/knowledgeObject/ark:/{naan}/{name}", "/shelf/ark:/{naan}/{name}"})
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


    @PostMapping(value = "/knowledgeObject/ark:/{naan}/{name}/result",
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Result> getResultByArkId(@RequestBody Map<String, Object> inputs, ArkId arkId) {

        KnowledgeObjectDTO ko;

        try {

            ko = localStorage.getObject(arkId);

        } catch (OTExecutionStackEntityNotFoundException e) {

            ko = objTellerInterface.checkOutByArkId(arkId);
            localStorage.saveObject(ko, arkId);
        }


        Result result = calculate(inputs, ko);


        result.setSource(arkId.getArkId());

        return new ResponseEntity<Result>(result, HttpStatus.OK);

    }

//    private Result calculateAThingUsingAnotherThingAndThenDoSomething(Map<String, Object> inputs, KnowledgeObjectDTO ko) throws OTExecutionStackException {
    Result calculate(Map<String, Object> inputs, KnowledgeObjectDTO ko) throws OTExecutionStackException {


        Result result = null;
        String errormessage;

        if (inputs != null && inputs.size() > 0) {

            log.info("Object Input Message and Output Message sent for conversion of RDF .  ");
            CodeMetadata metadata = convertor.covertInputOutputMessageToCodeMetadata(ko.inputMessage, ko.outputMessage);
            log.info("Object Input Message and Output Message conversion complete . Code Metadtata for Input and Output Message ");


            if (metadata != null) {
                errormessage = metadata.verifyInput(inputs);
                if (errormessage == null) {
                    String code = ko.payload.content;

                    Payload payload = ko.payload;


                    if (code != null) {
                        if (EngineType.PYTHON.toString().equalsIgnoreCase(payload.engineType)) {
                            log.info("Object payload is sent to Paython Adator for execution.  ");
                            result = adapter.execute(code, payload.functionName, inputs, metadata.getReturntype());
                        }
                    } else {
                        errormessage = "Unable to retrieve content of ko. ";
                        log.error("Payload content is NULL for ko with Object ");
                    }
                }
            } else {
                errormessage = "Unable to convert RDF metadata for ko .";
                log.error("Unable to convert RDF Metadata for ko with Object ");
            }
        } else {
            errormessage = "Either parameter inputs is missing";
            log.error("Parameter inputs is missing  in the input resquest");
        }
        if (errormessage != null || result == null) { // errormessaage has a value
            result = new Result();
            result.setErrorMessage(errormessage);
            result.setSuccess(0);
        }


        return result;
    }

    @DeleteMapping(value = {"/shelf/ark:/{naan}/{name}", "/knowledgeObject/ark:/{naan}/{name}"})
    public ResponseEntity<String> deleteObjectOnTheShelfByArkId(ArkId arkId) {

        if (localStorage.deleteObject(arkId)) {
            return new ResponseEntity<String>("Object with ArkId " + arkId + " is removed from the Shelf", HttpStatus.GONE);
        } else {
            return new ResponseEntity<String>("Unable to delete Object with Ark Id " + arkId + ".", HttpStatus.INTERNAL_SERVER_ERROR);
        }

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

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(OTExecutionStackException.class)
    @ResponseBody
    String
    handleBadRequest(HttpServletRequest req, Exception ex) {
        return req.getRequestURL() + " " + ex.getMessage();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(OTExecutionStackEntityNotFoundException.class)
    @ResponseBody
    String
    handleEntityNotFound(HttpServletRequest req, Exception ex) {
        return req.getRequestURL() + " " + ex.getMessage();
    }
}

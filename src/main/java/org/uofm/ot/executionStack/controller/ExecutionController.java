package org.uofm.ot.executionStack.controller;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.uofm.ot.executionStack.adapter.PythonAdapter;
import org.uofm.ot.executionStack.exception.OTExecutionStackEntityNotFoundException;
import org.uofm.ot.executionStack.exception.OTExecutionStackException;
import org.uofm.ot.executionStack.objectTellerLayer.ObjectTellerInterface;
import org.uofm.ot.executionStack.reposity.Shelf;
import org.uofm.ot.executionStack.transferObjects.ArkId;
import org.uofm.ot.executionStack.transferObjects.CodeMetadata;
import org.uofm.ot.executionStack.transferObjects.EngineType;
import org.uofm.ot.executionStack.transferObjects.KnowledgeObjectDTO;
import org.uofm.ot.executionStack.transferObjects.KnowledgeObjectDTO.Payload;
import org.uofm.ot.executionStack.transferObjects.Result;
import org.uofm.ot.executionStack.util.CodeMetadataConvertor;

/**
 *
 * Created by nggittle on 3/22/2017.
 */

@RestController
@CrossOrigin
public class ExecutionController {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private CodeMetadataConvertor convertor;
  @Autowired
  private PythonAdapter adapter;
  @Autowired
  private Shelf shelf;

  @PostMapping(value = "/knowledgeObject/ark:/{naan}/{name}/result",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @ResponseStatus(HttpStatus.OK)
  public Result getResultByArkId(@RequestBody Map<String, Object> inputs, ArkId arkId) {

    KnowledgeObjectDTO ko;

    ko = shelf.getObject(arkId);

    //TODO: If we need to auto-load objects then add to shelf controller

    Result result = validateAndExecute(inputs, ko);

    result.setSource(arkId.getArkId());

    return result;
  }

  /**
   * Returns true iff the KO is valid when tested against the supplied inputs
   * @param inputs the data to be calculated by the ko
   * @param payload the payload from the given knowledge object
   * @param metadata
   * @return true if the KO has metadata,
   * @throws OTExecutionStackException if the KO fails validation
   */
  private boolean isInputAndPayloadValid(Map<String, Object> inputs, Payload payload, CodeMetadata metadata) throws OTExecutionStackException {

    log.info("Checking for any inputs");
    if(inputs == null || inputs.size() < 1) {
      throw new OTExecutionStackException("No inputs given.");
    }

    log.info("Checking knowledge object against inputs " + inputs.toString());
    if (metadata == null) {
      throw new OTExecutionStackException("Unable to convert RDF metadata for ko.");
    }

    log.info("Checking meteadata for errors");
    String errorMessage = metadata.verifyInput(inputs);
    if (errorMessage != null) {
      throw new OTExecutionStackException("Error in converting RDF metadata for ko: " + errorMessage);
    }

    log.info("Checking payload for content");
    if (payload == null || payload.content == null) {
      throw new OTExecutionStackException("Payload content is NULL for ko");
    }

    log.info("Checking payload language");
    if (!EngineType.PYTHON.toString().equalsIgnoreCase(payload.engineType)) {
      throw new OTExecutionStackException("Expected Python payload but instead got " + payload.engineType);
    }

    return true;
  }

  Result validateAndExecute(Map<String, Object> inputs, KnowledgeObjectDTO ko) {

    Result result = null;
    CodeMetadata ioSpec;

    log.info("Object Input Message and Output Message sent for conversion of RDF .");
    ioSpec = convertor.covertInputOutputMessageToCodeMetadata(ko);
    log.info("Object Input Message and Output Message conversion complete . Code Metadata for Input and Output Message ");

    if (isInputAndPayloadValid(inputs, ko.payload, ioSpec)) {
      Payload payload = ko.payload;

      String code = payload.content;

      log.info("Object payload is sent to Python Adaptor for execution.");
      result = adapter.execute(inputs, payload, ioSpec.getReturntype() );
    }

    return result;
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(OTExecutionStackException.class)
  @ResponseBody String
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

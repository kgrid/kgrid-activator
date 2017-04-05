package org.uofm.ot.activator.services;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.uofm.ot.activator.adapter.PythonAdapter;
import org.uofm.ot.activator.domain.KnowledgeObject;
import org.uofm.ot.activator.exception.OTExecutionStackException;
import org.uofm.ot.activator.repository.Shelf;
import org.uofm.ot.activator.domain.ArkId;
import org.uofm.ot.activator.domain.EngineType;
import org.uofm.ot.activator.domain.KnowledgeObject.Payload;
import org.uofm.ot.activator.domain.Result;

/**
 * Created by nggittle on 3/31/17.
 */
@Service
public class ActivationService {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private Shelf shelf;
  @Autowired
  private IoSpecGenerator convertor;
  @Autowired
  private PythonAdapter adapter;


  public Result getResultByArkId(Map<String, Object> inputs, ArkId arkId) {

    KnowledgeObject ko;

    ko = shelf.getObject(arkId).getKo();

    //TODO: If we need to auto-load objects then add to shelf controller

    Result result = validateAndExecute(inputs, ko);

    result.setSource(arkId.getArkId());

    result.setMetadata(ko.metadata);

    return result;

  }

  /**
   * Returns true iff the KO is valid when tested against the supplied inputs
   * @param inputs the data to be calculated by the ko
   * @param payload the payload from the given knowledge object
   * @param ioSpec the specification that the payload will be validated against
   * @return true if the KO has valid ioSpec,
   * @throws OTExecutionStackException if the KO fails validation
   */
  private boolean isInputAndPayloadValid(Map<String, Object> inputs, Payload payload, ioSpec ioSpec) throws OTExecutionStackException {

    if(inputs == null || inputs.size() < 1) {
      throw new OTExecutionStackException("No inputs given.");
    }

    if (ioSpec == null) {
      throw new OTExecutionStackException("Unable to convert RDF ioSpec for ko.");
    }

    String errorMessage = ioSpec.verifyInput(inputs);
    if (errorMessage != null) {
      throw new OTExecutionStackException("Error in converting RDF ioSpec for ko: " + errorMessage);
    }

    if (payload == null || payload.content == null) {
      throw new OTExecutionStackException("Knowledge object payload content is NULL or empty");
    }

    if (!EngineType.PYTHON.toString().equalsIgnoreCase(payload.engineType)) {
      throw new OTExecutionStackException("Expected Python payload but instead got " + payload.engineType);
    }

    return true;
  }

  Result validateAndExecute(Map<String, Object> inputs, KnowledgeObject ko) {

    Result result = null;
    ioSpec ioSpec;

    log.info("Object Input Message and Output Message sent for conversion of RDF .");
    ioSpec = convertor.covertInputOutputMessageToCodeMetadata(ko);
    log.info("Object Input Message and Output Message conversion complete . Code Metadata for Input and Output Message ");

    if (isInputAndPayloadValid(inputs, ko.payload, ioSpec)) {
      Payload payload = ko.payload;

      log.info("Object payload is sent to Python Adaptor for execution.");
      result = adapter.execute( inputs, payload, ioSpec.getReturntype() );
    }

    return result;
  }

}

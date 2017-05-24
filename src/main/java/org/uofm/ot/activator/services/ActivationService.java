package org.uofm.ot.activator.services;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.uofm.ot.activator.adapter.JavaScriptAdapter;
import org.uofm.ot.activator.adapter.PythonAdapter;
import org.uofm.ot.activator.adapter.ServiceAdapter;
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
  private IoSpecGenerator converter;
  @Autowired
  private ApplicationContext context;


  public Result getResultByArkId(Map<String, Object> inputs, ArkId arkId) {

    KnowledgeObject ko;

    ko = shelf.getObject(arkId);

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

    return true;
  }

  Result validateAndExecute(Map<String, Object> inputs, KnowledgeObject ko) {

    Result result = new Result();
    ioSpec ioSpec;

    log.info("Object Input Message and Output Message sent for conversion of RDF .");
    ioSpec = converter.covertInputOutputMessageToCodeMetadata(ko);
    log.info("Object Input Message and Output Message conversion complete . Code Metadata for Input and Output Message ");

    if (isInputAndPayloadValid(inputs, ko.payload, ioSpec)) {

      log.info("Object payload is sent to Python Adapter for execution.");
      ServiceAdapter adapter = adapterFactory(EngineType.valueOf(ko.payload.engineType.toUpperCase()));
      result.setResult(adapter.execute(inputs, ko.payload.content, ko.payload.functionName, ioSpec.getReturnTypeAsClass()));
    }

    return result;
  }

  private ServiceAdapter adapterFactory(EngineType adapterLanguage) {
    switch(adapterLanguage) {
      case PYTHON:
        return context.getBean(PythonAdapter.class);
      case JS:
        return context.getBean(JavaScriptAdapter.class);
      case R:
        //return context.getBean(RAdapter.class);
      default:
        throw new OTExecutionStackException("Invalid adapter provided in object payload. Adapter " + adapterLanguage.toString() + " is not hooked up to this activator.");
    }
  }

}

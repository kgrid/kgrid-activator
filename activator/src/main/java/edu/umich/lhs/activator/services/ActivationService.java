package edu.umich.lhs.activator.services;

import edu.umich.lhs.activator.adapter.EnvironmentAdapter;
import edu.umich.lhs.activator.domain.ArkId;
import edu.umich.lhs.activator.domain.KnowledgeObject;
import edu.umich.lhs.activator.domain.Kobject;
import edu.umich.lhs.activator.domain.Payload;
import edu.umich.lhs.activator.domain.Result;
import edu.umich.lhs.activator.exception.ActivatorException;
import edu.umich.lhs.activator.repository.Shelf;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * Responsible for loading Adapters, matching payload with adapter,
 *   passing payload to adapter, and returning result.
 */
@Service
public class ActivationService implements ApplicationContextAware {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private final Shelf shelf;
  private final IoSpecGenerator converter;

  @Value("${activator.adapter.path}")
  private String adapterPath;

  private Map<String, Class> executionImplementations = new HashMap<>();

  @Autowired
  public ActivationService(Shelf shelf, IoSpecGenerator converter) {
    this.shelf = shelf;
    this.converter = converter;
  }

  private ApplicationContext applicationContext;

  public void setApplicationContext(ApplicationContext context) {
    applicationContext = context;
  }

  public String getAdapterPath() {
    return adapterPath;
  }

  public Result getResultByArkId(Map<String, Object> inputs, ArkId arkId) {

    Kobject kob;

    kob = shelf.getObject(arkId);

    //TODO: If we need to auto-load objects then add to shelf controller

    Result result = validateAndExecute(inputs, kob);

    result.setSource(arkId.getArkId());

    result.setMetadata(kob.metadata);

    return result;

  }

  /**
   * Returns true iff the KO is valid when tested against the supplied inputs
   * @param inputs the data to be calculated by the ko
   * @param payload the payload from the given knowledge object
   * @param ioSpec the specification that the payload will be validated against
   * @return true if the KO has valid ioSpec,
   * @throws ActivatorException if the KO fails validation
   */
  private boolean isInputAndPayloadValid(Map<String, Object> inputs, Payload payload, ioSpec ioSpec) throws ActivatorException {

    if(inputs == null || inputs.size() < 1) {
      throw new ActivatorException("No inputs given.");
    }

    if (ioSpec == null) {
      throw new ActivatorException("Unable to convert RDF ioSpec for ko.");
    }

    String errorMessage = ioSpec.verifyInput(inputs);
    if (errorMessage != null) {
      throw new ActivatorException("Error in converting RDF ioSpec for ko: " + errorMessage);
    }

    if (payload == null || payload.getContent() == null) {
      throw new ActivatorException("Knowledge object payload content is NULL or empty");
    }

    return true;
  }

  Result validateAndExecute(Map<String, Object> inputs, Kobject kob) {

    Result result = new Result();

    // Conversion from RDF to Java object was done at load time
    /*
    log.info("Object Input Message and Output Message sent for conversion of RDF .");
    ioSpec = converter.covertInputOutputMessageToCodeMetadata(ko);
    log.info("Object Input Message and Output Message conversion complete . Code Metadata for Input and Output Message ");
    */

    PayloadProviderValidator pValidator = new PayloadProviderValidator(kob);
    pValidator.verify();

    PayloadInputValidator inputValidator = new PayloadInputValidator(kob, inputs);


    // Problem with the structure of the provided payload
    if(!pValidator.verify()){
      throw new ActivatorException(pValidator.getMessage());
    }

    if(inputValidator.isValid()){
      log.info("Payload provider (kobject) validataed.");
      log.info("Object payload is sent to Adapter for execution.");

      try {
        Class adapter = adapterFactory(kob.getEngineType());
        EnvironmentAdapter environmentAdapterInstance = (EnvironmentAdapter)adapter.newInstance();
        applicationContext.getAutowireCapableBeanFactory().autowireBean(environmentAdapterInstance);

        Method execute = adapter.getDeclaredMethod("execute", Map.class, String.class, String.class, Class.class);
        Object resultObj = execute.invoke(environmentAdapterInstance, inputs, kob.getContent(), kob.getFunctionName(),
            kob.getReturnType());
        result.setResult(resultObj);
      } catch (IllegalAccessException | NoSuchMethodException | InstantiationException e) {
        throw new ActivatorException("Error invoking execute for payload " + e, e);
      } catch (InvocationTargetException invocationEx) {
        throw new ActivatorException("Error invoking execute due to internal adapter error: " + invocationEx.getCause(), invocationEx);
      }
    }
    else{
      throw new ActivatorException(inputValidator.getMessage());
    }

    return result;
  }

  private Class adapterFactory(String engineType) {

    if(executionImplementations.containsKey(engineType)) {
      return executionImplementations.get(engineType);
    }

    executionImplementations = reloadAdapterList();

    if(executionImplementations.containsKey(engineType)) {
      return executionImplementations.get(engineType);
    }

    throw new ActivatorException("Adapter for language " + engineType + " not found in internal adapters or at location " + adapterPath + " Please supply an adapter.");

  }

  public Map<String, Class> reloadAdapterList() {

    executionImplementations.clear(); // clear the list before reload so we don't run out of memory
    executionImplementations.putAll(loadExternalAdapters());
    executionImplementations.putAll(loadBuiltInAdapters());
    return executionImplementations;
  }

  private HashMap<String, Class> loadBuiltInAdapters() {
    HashMap<String, Class> adapters = new HashMap<>();

    ServiceLoader<EnvironmentAdapter> loader = ServiceLoader.load(EnvironmentAdapter.class);
    for(EnvironmentAdapter adapter : loader) {
      for(String language : adapter.supports()) {
        adapters.put(language.toUpperCase(), adapter.getClass());
      }
    }

    return adapters;
  }

  private HashMap<String, Class> loadExternalAdapters() {
    HashMap<String, Class> executionImp = new HashMap<>();

    File adapterDir = new File(adapterPath);
    if(adapterDir.listFiles() == null || adapterDir.listFiles().length == 0) {
      log.info("No adapters found in adapter directory " + adapterPath );
      return executionImp;
    }
    for (File classFile : adapterDir.listFiles()) {
      if (!classFile.getName().endsWith(".jar")) {
        continue;
      }
      try {
        URL[] urls = {new URL("jar:file:" + classFile + "!/")};

        URLClassLoader cl = new URLClassLoader(urls, EnvironmentAdapter.class.getClassLoader());

        ServiceLoader<EnvironmentAdapter> loader = ServiceLoader.load(EnvironmentAdapter.class, cl);
        for(EnvironmentAdapter adapter : loader) {
          for (String language : adapter.supports()) {
            executionImp.put(language.toUpperCase(), adapter.getClass());
          }
        }
      } catch (IOException ioE) {
        log.error("Cannot open jar file " + classFile + " IO error: " + ioE);
      }
    }
    return executionImp;
  }

}

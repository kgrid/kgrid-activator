package org.uofm.ot.activator.services;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.uofm.ot.activator.adapter.ServiceAdapter;
import org.uofm.ot.activator.domain.KnowledgeObject;
import org.uofm.ot.activator.exception.OTExecutionStackException;
import org.uofm.ot.activator.repository.Shelf;
import org.uofm.ot.activator.domain.ArkId;
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
  @Value("${stack.adapter.path:classpath:adapters}")
  private String adapterPath;

  private Map<String, Class> executionImplementations = new HashMap<>();


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

      log.info("Object payload is sent to Adapter for execution.");

      try {
        Class adapter = adapterFactory(ko.payload.engineType);
        Object serviceAdapterInstance = adapter.newInstance();
        Method execute = adapter.getDeclaredMethod("execute", Map.class, String.class, String.class, Class.class);
        Object resultObj = execute.invoke(serviceAdapterInstance, inputs, ko.payload.content, ko.payload.functionName,
            ioSpec.getReturnTypeAsClass());
        result.setResult(resultObj);
      } catch (IllegalAccessException | NoSuchMethodException | InstantiationException e) {
        throw new OTExecutionStackException("Error invoking execute for payload " + e, e);
      } catch (InvocationTargetException invocationEx) {
        throw new OTExecutionStackException("Error invoking execute due to internal adapter error: " + invocationEx.getCause(), invocationEx);
      }
    }

    return result;
  }

  private Class adapterFactory(String adapterLanguage) {

    String adapter = adapterLanguage.toUpperCase();
    if(executionImplementations.containsKey(adapter)) {
        return executionImplementations.get(adapter);
    } else {
      executionImplementations = loadAdapters();
      if(executionImplementations.containsKey(adapter)) {
        return executionImplementations.get(adapter);
      }
      throw new OTExecutionStackException("Adapter for language " + adapterLanguage + " not found at location " + adapterPath + " Please supply an adapter.");
    }
  }

  // Loads adapter classes contained in jar files from the user-specified adapter path
  private HashMap<String, Class> loadAdapters() {

    HashMap<String, Class> executionImp = new HashMap<>();

    try {
      File adapterDir;
      if(adapterPath.startsWith("classpath:")) {
        // Chop off the beginning "classpath:" and then get the file
        Resource adapterResource = new ClassPathResource(adapterPath.substring(adapterPath.indexOf(":") + 1));
        adapterDir = adapterResource.getFile();
      } else {
        adapterDir = new File(adapterPath);
      }
      if(adapterDir.listFiles() == null) {
        throw new OTExecutionStackException("Adapter directory " + adapterPath + " not found. Please correct the adapter directory location setting.");
      }
      if(adapterDir.listFiles().length == 0 ) {
        throw new OTExecutionStackException("No files found in the adapter directory " + adapterPath);
      }

      // Loop over every jar file in the specified adapter directory and load in every class that implements ServiceAdapter
      for (File classFile : adapterDir.listFiles()) {
        if (!classFile.getName().endsWith(".jar")) {
          continue;
        }
        JarFile adapterJar;
        try {
          adapterJar = new JarFile(classFile);
        } catch (ZipException zipE) {
          log.error("Cannot open jar file " + classFile + " Zip error: " + zipE);
          continue;
        }

        Enumeration<JarEntry> entryEnumeration = adapterJar.entries();
        URL[] urls = { new URL("jar:file:" + classFile + "!/")};
        URLClassLoader cl = URLClassLoader.newInstance(urls);

        while (entryEnumeration.hasMoreElements()){
          JarEntry entry = entryEnumeration.nextElement();
          Class<?> classToLoad;

          if(entry.isDirectory() || !entry.getName().endsWith(".class")) {
            continue;
          }
          String className = entry.getName().substring(0,entry.getName().length() - 6); // Length - 6 to chop off ending .class
          className = className.replace('/', '.'); // Make into package name instead of directory path
          try {
            classToLoad = cl.loadClass(className);
          } catch (ClassNotFoundException | NoClassDefFoundError classEx) {
            continue;
          }
          boolean implementsServiceAdapter = false;
          for (Class classInterface : classToLoad.getInterfaces()) {
            if(classInterface.getSimpleName().equals(ServiceAdapter.class.getSimpleName())){
              implementsServiceAdapter = true;
              break;
            }
          }
          if(implementsServiceAdapter) {
            try {
              Object serviceAdapterInstance = classToLoad.newInstance();
              Method supportsMethod = classToLoad.getDeclaredMethod("supports");
              List<String> supports = (List<String>)supportsMethod.invoke(serviceAdapterInstance);
              for (String language : supports) {
                if(executionImp.get(language) != null) {
                  log.warn("An adapter already exists for language " + language + "!! Undetermined behavior will occur with two adapters for the same language!");
                }
                executionImp.put(language.toUpperCase(), classToLoad);
                log.info("Loaded adapter for language " + language);
              }
            } catch (IllegalAccessException | NoSuchMethodException | InstantiationException ex) {
              log.info(ex.getMessage());
            }
          }
        }
      }
    } catch ( IOException | InvocationTargetException e) {
      throw new OTExecutionStackException("Something exploded while loading adapters: " + e, e);
    }

    if (executionImp.size() == 0) {
      throw new OTExecutionStackException("No valid adapters found. Please place adapters into the directory " + adapterPath);
    }
    return executionImp;
  }

  public Map<String, Class> loadAndGetAdapterList() {
    executionImplementations = loadAdapters();
    return executionImplementations;
  }

}

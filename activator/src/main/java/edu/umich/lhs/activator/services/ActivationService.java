package edu.umich.lhs.activator.services;

import edu.umich.lhs.activator.adapter.EnvironmentAdapter;
import edu.umich.lhs.activator.exception.ActivatorException;
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
import java.util.ServiceLoader;
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
import edu.umich.lhs.activator.domain.KnowledgeObject;
import edu.umich.lhs.activator.repository.Shelf;
import edu.umich.lhs.activator.domain.ArkId;
import edu.umich.lhs.activator.domain.KnowledgeObject.Payload;
import edu.umich.lhs.activator.domain.Result;

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
  @Value("${stack.adapter.path:${user.home}/adapters}")
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

    if (payload == null || payload.content == null) {
      throw new ActivatorException("Knowledge object payload content is NULL or empty");
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
        Object environmentAdapterInstance = adapter.newInstance();
        Method execute = adapter.getDeclaredMethod("execute", Map.class, String.class, String.class, Class.class);
        Object resultObj = execute.invoke(environmentAdapterInstance, inputs, ko.payload.content, ko.payload.functionName,
            ioSpec.getReturnTypeAsClass());
        result.setResult(resultObj);
      } catch (IllegalAccessException | NoSuchMethodException | InstantiationException e) {
        throw new ActivatorException("Error invoking execute for payload " + e, e);
      } catch (InvocationTargetException invocationEx) {
        throw new ActivatorException("Error invoking execute due to internal adapter error: " + invocationEx.getCause(), invocationEx);
      }
    }

    return result;
  }

  private Class adapterFactory(String adapterLanguage) {

    String adapter = adapterLanguage.toUpperCase();
    if(executionImplementations.containsKey(adapter)) {
        return executionImplementations.get(adapter);
    } else {
      executionImplementations.putAll(loadAdapters());
      executionImplementations.putAll(loadBuiltInAdapters());
      if(executionImplementations.containsKey(adapter)) {
        return executionImplementations.get(adapter);
      }
      throw new ActivatorException("Adapter for language " + adapterLanguage + " not found in internal adapters or at location " + adapterPath + " Please supply an adapter.");
    }
  }

  private HashMap<String, Class> loadBuiltInAdapters() {
    HashMap<String, Class> adapters = new HashMap<>();

    ServiceLoader<EnvironmentAdapter> loader = ServiceLoader.load(EnvironmentAdapter.class);
    for(EnvironmentAdapter adapter : loader) {
      for(String language : adapter.supports()) {
        adapters.put(language, adapter.getClass());
      }
    }

    return adapters;
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
      if(adapterDir == null || adapterDir.listFiles() == null || adapterDir.listFiles().length == 0) {
        log.info("No adapters found in adapter directory " + adapterPath );
        return executionImp;
      }
      // Loop over every jar file in the specified adapter directory and load in every class that implements EnvironmentAdapter
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
          boolean implementsEnvironmentAdapter = false;
          for (Class classInterface : classToLoad.getInterfaces()) {
            try {
              if (classInterface.getSimpleName().equals(EnvironmentAdapter.class.getSimpleName())) {
                implementsEnvironmentAdapter = true;
                break;
              }
            } catch (NoClassDefFoundError e) {
              log.warn(e.toString() + " when loading classes for " + classToLoad);
            }
          }
          if(implementsEnvironmentAdapter) {
            try {
              Object environmentAdapterInstance = classToLoad.newInstance();
              Method supportsMethod = classToLoad.getDeclaredMethod("supports");
              List<String> supports = (List<String>)supportsMethod.invoke(environmentAdapterInstance);
              for (String language : supports) {
                if(executionImp.get(language.toUpperCase()) != null) {
                  log.warn("Multiple adapters exist for language " + language + "!! Undetermined behavior will occur with two or more adapters for the same language!");
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
      throw new ActivatorException("Something exploded while loading adapters: " + e, e);
    }

    if (executionImp.size() == 0) {
      log.info("No valid adapters found in adapter directory " + adapterPath);
    }
    return executionImp;
  }

  public Map<String, Class> loadAndGetAdapterList() {
    executionImplementations.putAll(loadAdapters());
    executionImplementations.putAll(loadBuiltInAdapters());
    return executionImplementations;
  }

}

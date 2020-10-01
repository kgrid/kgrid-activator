package org.kgrid.activator.services;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.kgrid.adapter.api.ActivationContext;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.domain.ArkId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// Example for external adapter loading
public class ExternalAdapterLoaderTest {

  private final Logger log = LoggerFactory.getLogger(this.getClass());
  Path adapterPath;

  @Before
  public void setup() {

  }

//  @Test
//  @Profile("sample")
  public void runIt() {
    HashMap<String, Class> stuff = loadExternalAdapters();
  }

  private HashMap<String, Class> loadExternalAdapters() {
    HashMap<String, Class> executionImp = new HashMap<>();

    File adapterDir = Paths.get(".").toFile();

    List<URL> urls = new ArrayList<>();

    for (File potentialAdapterJarFile : adapterDir.listFiles()) {
      if (potentialAdapterJarFile.getName().endsWith(".jar")) {
        try {
          urls.add(new URL("jar:file:" + potentialAdapterJarFile + "!/"));
        } catch (MalformedURLException e) {
          e.printStackTrace();
        }
      }

      URLClassLoader externalClassLoader =
          new URLClassLoader(null, Adapter.class.getClassLoader());

      ServiceLoader<Adapter> loader = ServiceLoader.load(Adapter.class, externalClassLoader);
      for (Adapter adapter : loader) {
        log.info("Loading adapter type: {}", adapter.getClass().getName());
        adapter.getEngines().forEach(engine -> executionImp.put(engine, adapter.getClass()));
      }
    }
    return executionImp;
  }

  class TestAdapter implements Adapter {

    @Override
    public List<String> getEngines() {
      return Collections.singletonList("TestAdapter");
    }

    @Override
    public void initialize(ActivationContext activationContext) {

    }

    @Override
    public Executor activate(URI uri, URI uri1, JsonNode jsonNode) {
      return null;
    }

    @Override
    public String status() {
      return null;
    }
  }


}

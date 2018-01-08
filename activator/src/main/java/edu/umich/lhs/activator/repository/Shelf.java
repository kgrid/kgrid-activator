package edu.umich.lhs.activator.repository;

import com.fasterxml.jackson.core.JsonGenerationException;
import edu.umich.lhs.activator.domain.ArkId;
import edu.umich.lhs.activator.domain.Kobject;
import edu.umich.lhs.activator.domain.Metadata;
import edu.umich.lhs.activator.exception.ActivatorException;
import edu.umich.lhs.activator.exception.KONotFoundException;
import edu.umich.lhs.activator.services.KobjectImporter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

@Service
public class Shelf {

  private final Logger log = LoggerFactory.getLogger(Shelf.class);
  private final String BUILTIN_SHELF = "shelf/";
  private final String BUILTIN_SHELF_PATTERN = BUILTIN_SHELF + "**";
  private Map<ArkId, Kobject> inMemoryShelf = new HashMap<>();
  @Value("${activator.shelf.path}")
  private String localStoragePath;

  public String getShelfPath() {
    return localStoragePath;
  }

  public void saveObject(Kobject kob, ArkId arkId) throws ActivatorException {

    // Fill identifier and metadata if none exist.
    if (kob.metadata == null) {
      kob.metadata = new Metadata();
    }
    if (kob.metadata.getArkId() == null) {
      kob.metadata.setArkId(arkId);
    }

    try {
      File folderPath = new File(localStoragePath);

      if (!folderPath.exists()) {
        folderPath.mkdirs();
      }

      File resultFile = new File(folderPath, arkId.getFedoraPath());
      FileOutputStream ofs = new FileOutputStream(resultFile);
      RDFDataMgr.write(ofs, kob.getRdfModel(), RDFFormat.JSONLD);

      log.info("Object written to shelf: " + resultFile.getAbsolutePath());

      inMemoryShelf.put(arkId, kob);

    } catch (IOException e) {
      throw new ActivatorException(e);
    }
  }

  public boolean isBuiltinObject(ArkId arkId) {
    Resource knowledgeResource = new ClassPathResource(BUILTIN_SHELF + arkId.getFedoraPath());
    return knowledgeResource.exists();
  }

  public Kobject getObject(ArkId arkId) {
    if (!inMemoryShelf.containsKey(arkId)) {
      Kobject kob = loadAndDeserialize(arkId);
      inMemoryShelf.put(arkId, kob);
    }
    return inMemoryShelf.get(arkId);
  }

  // Convenience method
  private Kobject loadAndDeserialize(ArkId arkId) {
    Resource res = loadKobjectResource(arkId);
    return deserializeKobjectResource(res);
  }

  // Load serialized kobject as Resource
  private Resource loadKobjectResource(ArkId arkId) {

    File shelf = new File(localStoragePath);
    File knowledgeFile = new File(shelf, arkId.getFedoraPath());

    Resource knowledgeResource = new FileSystemResource(knowledgeFile);

    // Search order for kobject: shelf/path, shelf/path.json, built-in shelf
    if (!knowledgeResource.exists()) {
      knowledgeFile = new File(shelf, arkId.getFedoraPath() + ".json");
      knowledgeResource = new FileSystemResource(knowledgeFile);

      if (!knowledgeResource.exists()) {
        knowledgeResource = new ClassPathResource(BUILTIN_SHELF + arkId.getFedoraPath());

        if (!knowledgeResource.exists()) {
          throw new KONotFoundException("Object with arkId " + arkId + " not found.");
        }
      }
    }

    return knowledgeResource;
  }

  // Deserialize kobject resource to kobject
  private Kobject deserializeKobjectResource(Resource res) {
    Kobject kob;

    try {
      kob = KobjectImporter.jsonToKobject(res.getInputStream());
    } catch (JsonGenerationException e) {
      throw new ActivatorException(e);
    } catch (IOException e) {
      throw new KONotFoundException(e);
    }

    return kob;
  }

  public boolean deleteObject(ArkId arkId) {

    boolean success;
    File folderPath = new File(localStoragePath);

    File resultFile = new File(folderPath, arkId.getFedoraPath());
    success = resultFile.delete();
    log.info("Object deleted from shelf: " + resultFile.getAbsolutePath());

    if (success) {
      if (inMemoryShelf.containsKey(arkId)) {
        inMemoryShelf.remove(arkId);
      }
    }
    return success;
  }

  public List<Kobject> getAllObjects() {
    File folderPath = new File(localStoragePath);

    log.info("Reloading shelf: " + folderPath.getAbsolutePath());
    List<Resource> knowledgeObjectResources = new ArrayList<>();
    knowledgeObjectResources.addAll(getBuiltinClasspathResources());

    knowledgeObjectResources.addAll(getFilesystemResources());

    for (Resource res : knowledgeObjectResources) {
      String objectName = res.getFilename();
      String[] parts = objectName.split("[-\\.]"); // split on hyphens and periods
      if ((parts.length == 2 || parts.length == 3) && objectName.indexOf('.') != 0) {
        ArkId arkId = new ArkId(parts[0], parts[1]);
        getObject(arkId);
      } else {
        log.warn("Incorrectly named KO resource: " + res.getFilename());
      }
    }
    return new ArrayList<>(inMemoryShelf.values());
  }

  private List<Resource> getBuiltinClasspathResources() {
    List<Resource> koResources = new ArrayList<>();
    try {
      PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
      Resource[] objectLocation = resolver.getResources(BUILTIN_SHELF_PATTERN);
      if (objectLocation[0] != null && objectLocation[0].exists()) {
        koResources.addAll(Arrays.asList(objectLocation));
      }
    } catch (IOException e) {
      log.warn("Failed to get resources from the built-in shelf during startup " + e);
    }
    return koResources;
  }

  private List<Resource> getFilesystemResources() {
    List<Resource> koResources = new ArrayList<>();
    File shelfFolder = new File(localStoragePath);
    if (shelfFolder.isDirectory()) {
      for (File ko : shelfFolder.listFiles()) {
        koResources.add(new FileSystemResource(ko));
      }
    }
    return koResources;
  }

  @PostConstruct
  public void initBuiltinShelf() {
    getAllObjects();
  }

}

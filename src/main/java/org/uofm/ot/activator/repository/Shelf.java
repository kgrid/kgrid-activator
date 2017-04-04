package org.uofm.ot.activator.repository;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.uofm.ot.activator.domain.KnowledgeObject;
import org.uofm.ot.activator.domain.Metadata;
import org.uofm.ot.activator.exception.OTExecutionStackEntityNotFoundException;
import org.uofm.ot.activator.exception.OTExecutionStackException;
import org.uofm.ot.activator.domain.ArkId;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Shelf {

    private final Logger log = LoggerFactory.getLogger(Shelf.class);

    private Map<ArkId, SourcedKO> inMemoryShelf = new HashMap<>();

    @Value("${stack.shelf.path:.}")
    private String localStoragePath;

    @Value("${stack.shelf.name:shelf}")
    private String shelfName;

    private static final String BUILTIN_SHELF = "shelf/**";

    private File builtInDir;

    public void saveObject(KnowledgeObject dto, ArkId arkId) throws OTExecutionStackException {

        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writer();
            File folderPath = new File(localStoragePath, shelfName);

            if (folderPath.exists() == false) {
                folderPath.mkdirs();
            }

            File resultFile = new File(folderPath, arkId.getFedoraPath());

            writer.writeValue(resultFile, dto);
            log.info("Object written to shelf: " + resultFile.getAbsolutePath());
            if(dto.metadata == null) {
                dto.metadata = new Metadata();
            }
            if(dto.metadata.getArkId() == null) {
                dto.metadata.setArkId(arkId);
            }
            inMemoryShelf.put(arkId, new SourcedKO(dto, Source.USERGENERATED));

        } catch (JsonGenerationException e) {
            throw new OTExecutionStackException(e);
        } catch (JsonMappingException e) {
            throw new OTExecutionStackException(e);
        } catch (IOException e) {
            throw new OTExecutionStackException(e);
        }
    }

    public SourcedKO getObject(ArkId arkId) throws OTExecutionStackException {
        KnowledgeObject dto;
        try {
            if (!inMemoryShelf.containsKey(arkId)) {

                ObjectMapper mapper = new ObjectMapper();

                File folderPath = new File(localStoragePath, shelfName);
                Source source = Source.USERGENERATED;

                File resultFile = new File(folderPath, arkId.getFedoraPath());
                if (!resultFile.exists()) {
                    resultFile = new File(builtInDir, arkId.getFedoraPath());
                    source = Source.BUILTIN;
                    if(!resultFile.exists()) {
                        return new SourcedKO(null, Source.NONEXISTENT);
                    }
                }
                dto = mapper.readValue(resultFile, KnowledgeObject.class);
                if (dto.metadata == null)
                    dto.metadata = new Metadata();
                dto.metadata.setArkId(arkId);
                inMemoryShelf.put(arkId, new SourcedKO(dto, source));
            }
        } catch (JsonGenerationException e) {
            throw new OTExecutionStackException(e);
        } catch (JsonMappingException e) {
            throw new OTExecutionStackException(e);
        } catch (IOException e) {
            throw new OTExecutionStackEntityNotFoundException(e);
        }

        return inMemoryShelf.get(arkId);
    }

    public boolean deleteObject(ArkId arkId) {

        boolean success = false;
        File folderPath = new File(localStoragePath, shelfName);

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

    public List<Map<String, Object>> getAllObjects() {
        File folderPath = new File(localStoragePath, shelfName);
        log.info("Reloading shelf: " + folderPath.getAbsolutePath());

        List<Map<String, Object>> objectsOnTheShelf = new ArrayList<>();
        List<File> objectsOnShelf = new ArrayList<>();
        Set<ArkId> refreshedObjects = new HashSet<>();

        if(builtInDir != null && builtInDir.exists()) {
            File[] builtInObjects = builtInDir.listFiles();
            objectsOnShelf.addAll(Arrays.asList(builtInObjects));
        }
        if (folderPath != null && folderPath.exists()) {
            File[] objects = folderPath.listFiles();
            objectsOnShelf.addAll(Arrays.asList(objects));
        }
        for(File file : objectsOnShelf) {
            String objectName = file.getName();
            String[] parts = objectName.split("-");
            if(parts.length != 2) {
                throw new OTExecutionStackException("Incorrectly named KO file: " + file.getAbsolutePath());
            }
            ArkId arkId = new ArkId(parts[0], parts[1]);
            refreshedObjects.add(arkId);
        }

        for (ArkId arkId : refreshedObjects) {

            Map<String, Object> shelfEntry = new HashMap<>();

            SourcedKO sko = getObject(arkId);
            shelfEntry.put("url", sko.getKo().url);
            shelfEntry.put("metadata", sko.getKo().metadata);
            shelfEntry.put("source", sko.getSource().toString());

            objectsOnTheShelf.add(shelfEntry);
        }
        return objectsOnTheShelf;
    }

    @PostConstruct
    public void initBuiltinShelf() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] koFiles = resolver.getResources(BUILTIN_SHELF);
            if(koFiles[0] != null && koFiles[0].exists()) {
                builtInDir = koFiles[0].getFile().getParentFile();
                getAllObjects();
            }
        } catch (IOException ex) {
            throw new OTExecutionStackException(
                "Error trying to load default knowledge objects. "
                + "Check that classpath is configured correctly " + ex);
        }
    }

    public class SourcedKO {
        private KnowledgeObject ko;
        private Source source;

        SourcedKO(KnowledgeObject ko, Source source) {
            this.ko = ko;
            this.source = source;
        }

        public KnowledgeObject getKo() {
            return ko;
        }

        public Source getSource() {
            return source;
        }
    }

    public enum Source {
        BUILTIN("built-in"),
        USERGENERATED("user-generated"),
        NONEXISTENT("non-existent");

        private String source;

        Source(String source) { this.source = source; }

        @Override
        public String toString(){ return this.source; }
    }
}

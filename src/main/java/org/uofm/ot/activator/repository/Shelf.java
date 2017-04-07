package org.uofm.ot.activator.repository;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.uofm.ot.activator.domain.ArkId;
import org.uofm.ot.activator.domain.KnowledgeObject;
import org.uofm.ot.activator.domain.Metadata;
import org.uofm.ot.activator.exception.OTExecutionStackEntityNotFoundException;
import org.uofm.ot.activator.exception.OTExecutionStackException;

@Service
public class Shelf {

    private final Logger log = LoggerFactory.getLogger(Shelf.class);

    private Map<ArkId, SourcedKO> inMemoryShelf = new HashMap<>();

    @Value("${stack.shelf.path:.}")
    private String localStoragePath;

    @Value("${stack.shelf.name:shelf}")
    private String shelfName;

    private final String BUILTIN_SHELF = "shelf/";


    public void saveObject(KnowledgeObject dto, ArkId arkId) throws OTExecutionStackException {

        try {
            ObjectMapper mapper = new ObjectMapper().disable(MapperFeature.USE_ANNOTATIONS);
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
        } catch (IOException e) {
            throw new OTExecutionStackException(e);
        }
    }

    public boolean isBuiltinObject(ArkId arkId) {
        Resource knowledgeResource = new ClassPathResource(BUILTIN_SHELF + arkId.getFedoraPath());
        return knowledgeResource.exists();
    }

    public SourcedKO getObject(ArkId arkId) {
        if(!inMemoryShelf.containsKey(arkId)) {
            SourcedKO sko = loadAndDeserializeObject(arkId);
            inMemoryShelf.put(arkId, sko);
        }
        return inMemoryShelf.get(arkId);
    }

    private SourcedKO loadAndDeserializeObject(ArkId arkId) {
        KnowledgeObject ko;
        ObjectMapper mapper = new ObjectMapper();
        Source source = Source.USERGENERATED;

        Resource knowledgeResource = new FileSystemResource(  localStoragePath + "/" + shelfName + "/" + arkId.getFedoraPath());
        if(!knowledgeResource.exists()) {
            knowledgeResource = new ClassPathResource(BUILTIN_SHELF + arkId.getFedoraPath());
            source = Source.BUILTIN;
        }
        if(!knowledgeResource.exists()) {
            return null;
        }

        try {
            ko = mapper.readValue(knowledgeResource.getInputStream(), KnowledgeObject.class);
        } catch (JsonGenerationException e) {
            throw new OTExecutionStackException(e);
        } catch (IOException e) {
            throw new OTExecutionStackEntityNotFoundException(e);
        }
        return new SourcedKO(ko, source);
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

    public List<SourcedKO> getAllObjects() {
        File folderPath = new File(localStoragePath, shelfName);

        log.info("Reloading shelf: " + folderPath.getAbsolutePath());
        List<Resource> knowledgeObjectResources = new ArrayList<>();
        knowledgeObjectResources.addAll(getAllResources(Source.BUILTIN.location));
        File userShelf = new File(localStoragePath, shelfName);
        knowledgeObjectResources.addAll(getAllResources(userShelf.getAbsolutePath()));

        for(Resource ko : knowledgeObjectResources) {
            String objectName = ko.getFilename();
            String[] parts = objectName.split("-");
            if(parts.length == 2) {
                ArkId arkId = new ArkId(parts[0], parts[1]);
                getObject(arkId);
            } else {
                log.warn("Incorrectly named KO file: " + ko.getFilename());
            }

        }
        return new ArrayList<>(inMemoryShelf.values());
    }

    private List<Resource> getAllResources(String objectLocationPattern){
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        List<Resource> koResources = new ArrayList<>();
        try {
            Resource[] objectLocation = resolver.getResources(objectLocationPattern);

            if(objectLocation[0] != null && objectLocation[0].exists()) {
                koResources.addAll(Arrays.asList(objectLocation));
            }
        } catch (IOException e) {
            log.error("Error gathering knowledge objects from filesystem at location matching pattern " + objectLocationPattern);
        }
        return koResources;
    }

    @PostConstruct
    public void initBuiltinShelf() {

        getAllObjects();
    }

    public class SourcedKO extends KnowledgeObject{
        private Source source;

        SourcedKO(KnowledgeObject ko, Source source){
            this.inputMessage = ko.inputMessage;
            this.outputMessage = ko.outputMessage;
            this.metadata = ko.metadata;
            this.payload = ko.payload;
            this.url = ko.url;
            this.source = source;
        }

        public Source getSource() {
            return source;
        }
    }

    public enum Source {
        BUILTIN("built-in", "/shelf/**"),
        USERGENERATED("user-generated", "file:**/shelf*");

        private String source;

        private String location;

        Source(String source, String location) { this.source = source; this.location = location; }

        @Override
        public String toString(){ return this.source; }
    }
}

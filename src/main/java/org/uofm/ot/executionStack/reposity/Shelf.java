package org.uofm.ot.executionStack.reposity;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.uofm.ot.executionStack.exception.OTExecutionStackEntityNotFoundException;
import org.uofm.ot.executionStack.exception.OTExecutionStackException;
import org.uofm.ot.executionStack.transferObjects.ArkId;
import org.uofm.ot.executionStack.transferObjects.KnowledgeObjectDTO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class Shelf {

    private Map<ArkId, KnowledgeObjectDTO> inMemoryShelf = new HashMap<ArkId, KnowledgeObjectDTO>();

    @Value("${stack.localStoragePath:.}")
    private String localStoragePath;

    @Value("${stack.shelfName:shelf}")
    private String shelfName;

    public void saveObject(KnowledgeObjectDTO dto, ArkId arkId) throws OTExecutionStackException {

        try {

            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writer();

            File folderPath = new File(localStoragePath, shelfName);

            if (folderPath.exists() == false) {
                folderPath.mkdirs();
            }

            File resultFile = new File(folderPath, arkId.getFedoraPath());

            writer.writeValue(resultFile, dto);
            inMemoryShelf.put(arkId, dto);

        } catch (JsonGenerationException e) {
            throw new OTExecutionStackException(e);
        } catch (JsonMappingException e) {
            throw new OTExecutionStackException(e);
        } catch (IOException e) {
            throw new OTExecutionStackException(e);
        }

    }


    public KnowledgeObjectDTO getObject(ArkId arkId) throws OTExecutionStackException {
        KnowledgeObjectDTO dto = null;
        try {

            if (!inMemoryShelf.containsKey(arkId)) {
                ObjectMapper mapper = new ObjectMapper();

                File folderPath = new File(localStoragePath, shelfName);

                File resultFile = new File(folderPath, arkId.getFedoraPath());

                dto = (KnowledgeObjectDTO) mapper.readValue(resultFile, KnowledgeObjectDTO.class);
            } else
                dto = inMemoryShelf.get(arkId);
        } catch (JsonGenerationException e) {
            throw new OTExecutionStackException(e);
        } catch (JsonMappingException e) {
            throw new OTExecutionStackException(e);
        } catch (IOException e) {
            throw new OTExecutionStackEntityNotFoundException(e);
        }

        return dto;
    }


    public boolean deleteObject(ArkId arkId) {

        boolean success = false;
        File folderPath = new File(localStoragePath, shelfName);

        File resultFile = new File(folderPath, arkId.getFedoraPath());
        success = resultFile.delete();
        if (success) {
            if (inMemoryShelf.containsKey(arkId)) {
                inMemoryShelf.remove(arkId);
            }
        }
        return success;
    }

    public List<Map<String, String>> getAllObjects() {
        File folderPath = new File(localStoragePath, shelfName);

        List<Map<String, String>> objectsOnTheShelf = new ArrayList<Map<String, String>>();

        ObjectMapper mapper = new ObjectMapper();

        if (folderPath.exists()) {
            File[] objects = folderPath.listFiles();
            for (File file : objects) {
                String objectName = file.getName();
                String[] parts = objectName.split("-");
                ArkId arkId = new ArkId(parts[0], parts[1]);
                if (!inMemoryShelf.containsKey(arkId)) {
                    try {
                        KnowledgeObjectDTO dto = (KnowledgeObjectDTO) mapper.readValue(file, KnowledgeObjectDTO.class);
                        inMemoryShelf.put(arkId, dto);
                    } catch (IOException e) {
                        throw new OTExecutionStackException(e);
                    }
                }
            }

            for (ArkId arkId : inMemoryShelf.keySet()) {
                Map<String, String> shelfEntry = new HashMap<String, String>();

                shelfEntry.put("ArkId", arkId.getArkId());
                shelfEntry.put("URL", inMemoryShelf.get(arkId).url);

                objectsOnTheShelf.add(shelfEntry);
            }
        }
        return objectsOnTheShelf;
    }
}

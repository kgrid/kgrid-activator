package org.uofm.ot.executionStack.reposity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.uofm.ot.executionStack.exception.OTExecutionStackException;
import org.uofm.ot.executionStack.transferObjects.ArkId;
import org.uofm.ot.executionStack.transferObjects.KnowledgeObjectDTO;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;


@Service
public class Shelf {
	
	private Map<String,KnowledgeObjectDTO> inMemoryShelf = new HashMap<String,KnowledgeObjectDTO>(); 
	
	@Value("${executionStack.localStoragePath:}")
	private String localStoragePath;
	
	public void saveObject(KnowledgeObjectDTO dto, ArkId arkId) throws OTExecutionStackException {
		
		try {

			ObjectMapper mapper = new ObjectMapper();
			ObjectWriter writer = mapper.writer();
			
			File resultFile = new File(localStoragePath, arkId.getFedoraPath()); 

			writer.writeValue(resultFile, dto);

		} catch (JsonGenerationException e) {
			throw new OTExecutionStackException(e);
		} catch (JsonMappingException e) {
			throw new OTExecutionStackException(e);
		} catch (IOException e) {
			throw new OTExecutionStackException(e);
		}
		
	}
	
	
	public KnowledgeObjectDTO getObject( ArkId arkId) throws OTExecutionStackException {
		KnowledgeObjectDTO dto = null;
		try {

			ObjectMapper mapper = new ObjectMapper();	
			
			File resultFile = new File(localStoragePath, arkId.getFedoraPath()); 

			dto = (KnowledgeObjectDTO) mapper.readValue(resultFile, KnowledgeObjectDTO.class);

		} catch (JsonGenerationException e) {
			throw new OTExecutionStackException(e);
		} catch (JsonMappingException e) {
			throw new OTExecutionStackException(e);
		} catch (IOException e) {
			throw new OTExecutionStackException(e);
		}
		 
		return dto;
	}
	

	public void deleteObject( ArkId arkId)  {
		
		File resultFile = new File(localStoragePath, arkId.getFedoraPath()); 

		resultFile.delete();
	}
}

package org.uofm.ot.executionStack.objectTellerLayer;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.uofm.ot.executionStack.transferObjects.ArkId;
import org.uofm.ot.executionStack.transferObjects.KnowledgeObjectDTO;


@Service
public class ObjectTellerInterface  {

	public KnowledgeObjectDTO checkOutByArkId(ArkId arkId) {
		RestTemplate rt = new RestTemplate();

		ResponseEntity<KnowledgeObjectDTO> response = rt.getForEntity(
				getAbsoluteObjectUrl(arkId)+"/complete",
				KnowledgeObjectDTO.class);
		
			
		KnowledgeObjectDTO object = response.getBody() ; 
		
		object.url = getAbsoluteObjectUrl(arkId) ; 

		return object;
	}


	@Value("${library.absolutePath:}")
	String libraryAbsolutePath;

	@Value("${library.relativePath:/ObjectTeller}")
	String libraryRelativePath;

	public String getLibraryPath() {

		String path;

		ServletUriComponentsBuilder uriBuilder = ServletUriComponentsBuilder.fromCurrentRequest();

		if (libraryAbsolutePath.isEmpty()) {
			path = uriBuilder.replacePath(libraryRelativePath).toUriString();
		} else {
			path = libraryAbsolutePath;
		}

		return path;
	}


	public String getAbsoluteObjectUrl(ArkId arkId) {
		return getLibraryPath()+"/knowledgeObject/" +arkId.getArkId();
	}
}

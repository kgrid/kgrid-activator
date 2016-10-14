package org.uofm.ot.executionStack.objectTellerLayer;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.uofm.ot.executionStack.exception.OTExecutionStackException;
import org.uofm.ot.executionStack.transferObjects.ArkId;
import org.uofm.ot.executionStack.transferObjects.KnowledgeObjectDTO;


@Service
public class ObjectTellerInterface  {
	
	@Value(value = "${OBJECTTELLER_PATH}")
	private String OBJECTTELLER_PATH;
	
	
	public KnowledgeObjectDTO checkOutByArkId(ArkId arkId) {
		RestTemplate rt = new RestTemplate();

		ResponseEntity<KnowledgeObjectDTO> response = rt.getForEntity(
				OBJECTTELLER_PATH+"/knowledgeObject/"+arkId.getArkId()+"/complete",

				KnowledgeObjectDTO.class);
		
			
		KnowledgeObjectDTO object = response.getBody() ; 

		return object;
	}


	public String getOBJECTTELLER_PATH() {
		return OBJECTTELLER_PATH;
	}

}

package org.uofm.ot.executionStack.objectTellerLayer;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.uofm.ot.executionStack.exception.OTExecutionStackException;
import org.uofm.ot.executionStack.transferObjects.KnowledgeObjectDTO;


@Service
public class ObjectTellerInterface  {
	
	@Value(value = "${OBJECTTELLER_PATH}")
	private String OBJECTTELLER_PATH;
	
	
	public KnowledgeObjectDTO checkOutByArkId(String objectArkId) throws OTExecutionStackException{
		RestTemplate rt = new RestTemplate();

		ResponseEntity<KnowledgeObjectDTO> response = rt.getForEntity(
				OBJECTTELLER_PATH+"/knowledgeObject/"+objectArkId+"/complete",

				KnowledgeObjectDTO.class);
		
		KnowledgeObjectDTO object = response.getBody() ; 

		return object;
	}

}

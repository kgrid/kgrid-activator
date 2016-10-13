package org.uofm.ot.executionStack.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uofm.ot.executionStack.adapter.PythonAdapter;
import org.uofm.ot.executionStack.exception.OTExecutionStackException;
import org.uofm.ot.executionStack.objectTellerLayer.ObjectTellerInterface;
import org.uofm.ot.executionStack.transferObjects.*;
import org.uofm.ot.executionStack.transferObjects.KnowledgeObjectDTO.Payload;
import org.uofm.ot.executionStack.util.CodeMetadataConvertor;

import java.util.HashMap;
import java.util.Map;


@RestController
public class ExecutionStackController {

	private Map<ArkId,KnowledgeObjectDTO> shelf = new HashMap<ArkId,KnowledgeObjectDTO>();

	@Autowired
	private ObjectTellerInterface objTellerInterface;

	@Autowired
	private CodeMetadataConvertor convertor;

	@Autowired
	private PythonAdapter adapter;

	@PutMapping(path={"/knowledgeObject/ark:/{naan}/{name}", "/shelf/ark:/{naan}/{name}"})
	public ResponseEntity<String> checkOutObject(ArkId arkId) {
		try {
			KnowledgeObjectDTO dto = objTellerInterface.checkOutByArkId(arkId);
			shelf.put(arkId, dto);
			ResponseEntity<String> result = new ResponseEntity<String>("Object Added on the shelf",HttpStatus.OK);
			return result;
		} catch (OTExecutionStackException e) {
			ResponseEntity<String> result = new ResponseEntity<String>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
			return result;
		}
	}



	@GetMapping(path={"/knowledgeObject", "/shelf"})
	public ResponseEntity<Map<ArkId,KnowledgeObjectDTO>> retrieveObjectsOnShelf() {
		return new ResponseEntity<Map<ArkId,KnowledgeObjectDTO>>(shelf,HttpStatus.OK);
	}

	@RequestMapping(value = "/knowledgeObject/ark:/{naan}/{name}/result", method = RequestMethod.POST,
			consumes = {MediaType.APPLICATION_JSON_VALUE},
			produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Result> getResultByArkId(@RequestBody Map<String,Object> content,ArkId arkId) throws OTExecutionStackException  {

		if (!shelf.containsKey(arkId) ) {
			KnowledgeObjectDTO dto = objTellerInterface.checkOutByArkId(arkId);
			shelf.put(arkId, dto);
		}
		
		KnowledgeObjectDTO object = shelf.get(arkId);
		
		Result result = calculate(content, object);
		
		result.setSource(arkId.getArkId());

		return new ResponseEntity<Result>(result, HttpStatus.OK);
	}

	private Result calculate(Map<String,Object> map , KnowledgeObjectDTO object) throws OTExecutionStackException  {

		Result result = null;
		String errormessage;

		if( map != null &&  map.size() > 0){


			CodeMetadata metadata = convertor.covertInputOutputMessageToCodeMetadata(object.inputMessage, object.outputMessage);

			if(metadata != null){
				errormessage = metadata.verifyInput( map);
				if(errormessage == null){
					String chunk = object.payload.content;

					Payload payload = object.payload;


					if( chunk != null) {
						if( EngineType.PYTHON.toString().equalsIgnoreCase(payload.engineType)){
							result = adapter.executeString(chunk, payload.functionName,map,metadata.getReturntype());
						}
					} else 
						errormessage = "Unable to retrieve content of object. ";
				}
			} else 
				errormessage = "Unable to convert RDF metadata for object .";

		} else
			errormessage = "Either object id or parameter map is missing";

		if(errormessage != null || result == null){ // errormessaage has a value
			result = new Result();
			result.setErrorMessage(errormessage);
			result.setSuccess(0);
		}

		

		return result ; 
	}




}

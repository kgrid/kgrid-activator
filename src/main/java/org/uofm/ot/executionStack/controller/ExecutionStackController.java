package org.uofm.ot.executionStack.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.uofm.ot.executionStack.adapter.PythonAdapter;
import org.uofm.ot.executionStack.exception.OTExecutionStackException;
import org.uofm.ot.executionStack.objectTellerLayer.ObjectTellerInterface;
import org.uofm.ot.executionStack.transferObjects.ArkId;
import org.uofm.ot.executionStack.transferObjects.CodeMetadata;
import org.uofm.ot.executionStack.transferObjects.EngineType;
import org.uofm.ot.executionStack.transferObjects.KnowledgeObjectDTO;
import org.uofm.ot.executionStack.transferObjects.KnowledgeObjectDTO.Payload;
import org.uofm.ot.executionStack.transferObjects.Result;
import org.uofm.ot.executionStack.util.CodeMetadataConvertor;


@RestController
public class ExecutionStackController {

	private Map<ArkId,KnowledgeObjectDTO> shelf = new HashMap<ArkId,KnowledgeObjectDTO>();

	@Autowired
	private ObjectTellerInterface objTellerInterface;

	@Autowired
	private CodeMetadataConvertor convertor;

	@Autowired
	private PythonAdapter adapter;

	@PutMapping(path="/execution-stack/ark:/{naan}/{name}/check-out")
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



	@GetMapping(path="/execution-stack/checked-out-objects")
	public ResponseEntity<Map<ArkId,KnowledgeObjectDTO>> retrieveObjectsOnShelf() {
		return new ResponseEntity<Map<ArkId,KnowledgeObjectDTO>>(shelf,HttpStatus.OK);
	}

	@RequestMapping(value = "/execution-stack/ark:/{naan}/{name}/result", method = RequestMethod.POST,
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

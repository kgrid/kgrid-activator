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

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@CrossOrigin
public class ExecutionStackController {

	private Map<ArkId,KnowledgeObjectDTO> shelf = new HashMap<ArkId,KnowledgeObjectDTO>();

	@Autowired
	private ObjectTellerInterface objTellerInterface;

	@Autowired
	private CodeMetadataConvertor convertor;

	@Autowired
	private PythonAdapter adapter;
	
	private static final String NAME_TO_THING_ADD = "http://n2t.net/";

	@PutMapping(path={"/knowledgeObject/ark:/{naan}/{name}", "/shelf/ark:/{naan}/{name}"})
	public ResponseEntity<String> checkOutObjectByArkId(ArkId arkId) throws OTExecutionStackException{
		try {
			KnowledgeObjectDTO dto = objTellerInterface.checkOutByArkId(arkId);
			shelf.put(arkId, dto);
			ResponseEntity<String> result = new ResponseEntity<String>("Object Added on the shelf",HttpStatus.OK);
			return result;
		} catch(Exception e) {
			throw new OTExecutionStackException("Not able to find the object with ArkId: "+arkId, e);
		}
	}


	
	@PutMapping (consumes = {MediaType.APPLICATION_JSON_VALUE}, 
			value={"/knowledgeObject/ark:/{naan}/{name}", "/shelf/ark:/{naan}/{name}"})
	public ResponseEntity<String> checkOutObject(ArkId arkId, @RequestBody KnowledgeObjectDTO dto ) throws OTExecutionStackException{
		try {
			dto.url = NAME_TO_THING_ADD + arkId ; 
			shelf.put(arkId, dto);
			ResponseEntity<String> result = new ResponseEntity<String>("Object Added on the shelf",HttpStatus.OK);
			return result;
		} catch(Exception e) {
			throw new OTExecutionStackException("Not able to find the object with ArkId: "+arkId, e);
		}
	}

	@GetMapping(path={"/knowledgeObject", "/shelf"})
	public List <Map<String,String>> retrieveObjectsOnShelf()  {
		List<Map<String,String>> objectsOnTheShelf = new ArrayList<Map<String,String>>();
		
		for (ArkId arkId : shelf.keySet()) {
			Map <String,String> shelfEntry = new HashMap<String,String>();

			shelfEntry.put("ArkId", arkId.getArkId());			
			shelfEntry.put("URL", shelf.get(arkId).url);
			
			objectsOnTheShelf.add(shelfEntry);
			
		}
		
		return objectsOnTheShelf ;
	}


	@PostMapping(value = "/knowledgeObject/ark:/{naan}/{name}/result",
			consumes = {MediaType.APPLICATION_JSON_VALUE},
			produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Result> getResultByArkId(@RequestBody Map<String,Object> content,ArkId arkId)  {

		KnowledgeObjectDTO object ;
		try {
			if (!shelf.containsKey(arkId) ) {
				KnowledgeObjectDTO dto = objTellerInterface.checkOutByArkId(arkId);
				shelf.put(arkId, dto);
			}

			object = shelf.get(arkId);
		} catch(Exception e) {
			throw new OTExecutionStackException("Not able to find the object with ArkId: "+arkId, e);
		}

		Result result = calculate(content, object);
		
		
		result.setSource(arkId.getArkId());

		return new ResponseEntity<Result>(result, HttpStatus.OK);
		
	}
	
	@DeleteMapping(value ={ "/shelf/ark:/{naan}/{name}", "/knowledgeObject/ark:/{naan}/{name}"})
	public ResponseEntity<String> getResultByArkId(ArkId arkId)  {

		if(shelf.containsKey(arkId)){
			shelf.remove(arkId);
			return new ResponseEntity<String>("Object with ArkId "+arkId+" is removed from the Shelf", HttpStatus.GONE);
		} else {
			return new ResponseEntity<String>("Object with Ark Id "+arkId+" is not on the shelf. ", HttpStatus.BAD_REQUEST);
		}
		
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

	@GetMapping(path={"/knowledgeObject/ark:/{naan}/{name}", "/shelf/ark:/{naan}/{name}"})
	public ResponseEntity<KnowledgeObjectDTO> retrieveObjectOnShelf(ArkId arkId)  {
		 
		if(shelf.containsKey(arkId)){
			KnowledgeObjectDTO dto = shelf.get(arkId);
			return new ResponseEntity<KnowledgeObjectDTO>(dto, HttpStatus.OK);
		} else {
			return new ResponseEntity<KnowledgeObjectDTO>( HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping(path={"/knowledgeObject/ark:/{naan}/{name}/payload", "/shelf/ark:/{naan}/{name}/payload"})
	public ResponseEntity<Payload> retrieveObjectPayload(ArkId arkId)  {
		 
		if(shelf.containsKey(arkId)){
			KnowledgeObjectDTO dto = shelf.get(arkId);
			return new ResponseEntity<Payload>(dto.payload, HttpStatus.OK);
		} else {
			return new ResponseEntity<Payload>( HttpStatus.NOT_FOUND);
		}
	}
	
	@GetMapping(path={"/knowledgeObject/ark:/{naan}/{name}/payload/content", "/shelf/ark:/{naan}/{name}/payload/content"})
	public ResponseEntity<String> retrievePayloadContent(ArkId arkId)  {
		 
		if(shelf.containsKey(arkId)){
			KnowledgeObjectDTO dto = shelf.get(arkId);
			if(dto.payload != null)
				return new ResponseEntity<String>(dto.payload.content, HttpStatus.OK);
			else
				return new ResponseEntity<String>("Object with ArkId "+arkId+" is on the shelf with no payload.", HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<String>("Object with ArkId "+arkId+" is not on the shelf.", HttpStatus.NOT_FOUND);
		}
	}


	@GetMapping("/whereami")
	public Map<String, String> where(HttpServletRequest req) {

		Map<String, String> here = new HashMap<>();

//		here.put("req.getRequestURL()",req.getRequestURL().toString());

		here.put("objTellerInterface.where()", objTellerInterface.getLibraryPath());

		return here;
	}

}

package org.uofm.ot.executionStack.controller;

import java.util.ArrayList;
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
import org.uofm.ot.executionStack.transferObjects.DataType;
import org.uofm.ot.executionStack.transferObjects.EngineType;
import org.uofm.ot.executionStack.transferObjects.KnowledgeObjectDTO;
import org.uofm.ot.executionStack.transferObjects.KnowledgeObjectDTO.Payload;
import org.uofm.ot.executionStack.transferObjects.ParamDescription;
import org.uofm.ot.executionStack.transferObjects.Result;
import org.uofm.ot.executionStack.util.CodeMetadataConvertor;




@RestController
public class ExecutionStackController {

	private Map<String,KnowledgeObjectDTO> shelf = new HashMap<String,KnowledgeObjectDTO>();
	
	@Autowired
	private ObjectTellerInterface objTellerInterface;
	
	@Autowired
	private CodeMetadataConvertor convertor;
	
	@PutMapping(path="/execution-stack/ark:/{naan}/{name}/check-out")
	public ResponseEntity<String> checkOutObject(ArkId arkId) {
		try {
			KnowledgeObjectDTO dto = objTellerInterface.checkOutByArkId(arkId.getArkId());
			shelf.put(arkId.getArkId(), dto);
			ResponseEntity<String> result = new ResponseEntity<String>("Object Added on the shelf",HttpStatus.OK);
			return result;
		} catch (OTExecutionStackException e) {
			ResponseEntity<String> result = new ResponseEntity<String>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
			return result;
		}
	}
	
	
	
	@GetMapping(path="/execution-stack/checked-out-objects")
	public ResponseEntity<Map<String,KnowledgeObjectDTO>> retrieveObjectsOnShelf() {
		return new ResponseEntity<Map<String,KnowledgeObjectDTO>>(shelf,HttpStatus.OK);
	}
	
	@RequestMapping(value = "/execution-stack/ark:/{naan}/{name}/result", method = RequestMethod.POST,
			consumes = {MediaType.APPLICATION_JSON_VALUE},
			produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Result> getResultByArkId(@RequestBody Map<String,Object> content,ArkId arkId) throws OTExecutionStackException  {

		Result result = calculate(content, arkId);

		return new ResponseEntity<Result>(result, HttpStatus.OK);
	}
	
	private Result calculate(Map<String,Object> map , ArkId arkId) throws OTExecutionStackException  {
		
		Result result = null;
		String errormessage;
		String title = null;
		boolean objectIsCheckedOut = false;
		
		String uri = arkId.getArkId();

		if(uri != null && map != null && !uri.isEmpty() && map.size() > 0){
			objectIsCheckedOut = shelf.containsKey(uri);
			if ( objectIsCheckedOut ) {

				KnowledgeObjectDTO object = shelf.get(uri);

				CodeMetadata metadata = convertor.covertInputOutputMessageToCodeMetadata(object.inputMessage, object.outputMessage);

				if(metadata != null){
					errormessage = verifyInput(metadata, map);
					if(errormessage == null){
						String chunk = object.payload.content;

						Payload payload = object.payload;


			     			if( chunk != null) {
							if( EngineType.PYTHON.toString().equalsIgnoreCase(payload.engineType)){
								PythonAdapter adapter = new PythonAdapter();
								result = adapter.executeString(chunk, payload.functionName,map,metadata.getReturntype());
}
						} else 
							errormessage = "Unable to retrieve content of object with id: "+ arkId.getArkId();
					}
				} else 
					errormessage = "Unable to convert RDF metadata for object with id:"+ arkId.getArkId();
			} else 
				errormessage = "Object with id: "+ arkId.getArkId() +" is not checked out on the shelf";
		} else
			errormessage = "Either object id or parameter map is missing";

		if(errormessage != null || result == null){ // errormessaage has a value
			result = new Result();
			result.setErrorMessage(errormessage);
			result.setSuccess(0);
		}

		result.setSource(title);
		
		return result ; 
	}


	private String verifyInput(CodeMetadata codeMetadata, Map<String,Object> ipParams){
		String errorMessage= null;
		if(codeMetadata.getNoOfParams() != ipParams.size()){
			errorMessage = "Number of input parameters should be "+codeMetadata.getNoOfParams()+".";
		}

		for (ParamDescription param : codeMetadata.getParams()) {
			if(!ipParams.containsKey(param.getName())){
				if(errorMessage == null)
					errorMessage= " Input parameter "+param.getName()+" is missing.";
				else
					errorMessage = errorMessage + " Input parameter "+param.getName()+" is missing.";
				break;
			}
		}

		if(errorMessage == null)
			errorMessage = verifyParameters(codeMetadata.getParams(),ipParams);

		return errorMessage;
	}
	
	private String verifyParameters(ArrayList<ParamDescription> list, Map<String,Object> params) {
		String error = null;
		for (ParamDescription paramDescription : list) {
			Object obj = params.get(paramDescription.getName());

			if (DataType.INT == paramDescription.getDatatype()){
				try {
					Integer value = Integer.parseInt(obj.toString());
					if(paramDescription.getMin() != null){
						if(value < paramDescription.getMin()) {
							error = " Parameter "+paramDescription.getName()+" should be of minimum value "+paramDescription.getMin();
							break;
						}
					}

					if(paramDescription.getMax() != null){
						if(value > paramDescription.getMax()) {
							error = " Parameter "+paramDescription.getName()+" should be less than maximum allowed value "+paramDescription.getMax();
							break;
						}
					}
					params.replace(paramDescription.getName(), value);
				} catch (NumberFormatException e){
					e.printStackTrace();
					error = " Parameter "+paramDescription.getName()+" should be of type INT";
					break;
				}
			} else {
				if(DataType.FLOAT == paramDescription.getDatatype()){
					try {
						Float value = Float.parseFloat(obj.toString());
						if(paramDescription.getMin() != null){
							if( value < paramDescription.getMin()){
								error = " Parameter "+paramDescription.getName()+" should be of minimum value "+paramDescription.getMin();
								break;
							}
						}

						if(paramDescription.getMax() != null){
							if(value > paramDescription.getMax()){
								error = " Parameter "+paramDescription.getName()+" should be less than maximum allowed value "+paramDescription.getMax();
								break;
							}
						}
					} catch (NumberFormatException e){
						e.printStackTrace();
						error = " Parameter "+paramDescription.getName()+" should be of type FLOAT";
						break;
					}
				} else {
					if(DataType.LONG == paramDescription.getDatatype()){
						try {
							Long value = Long.parseLong(obj.toString());
							if(paramDescription.getMin() != null){
								if( value < paramDescription.getMin()){
									error = " Parameter "+paramDescription.getName()+" should be of minimum value "+paramDescription.getMin();
									break;
								}
							}

							if(paramDescription.getMax() != null){
								if(value > paramDescription.getMax()){
									error = " Parameter "+paramDescription.getName()+" should be less than maximum allowed value "+paramDescription.getMax();
									break;
								}
							}
						} catch (NumberFormatException e){
							e.printStackTrace();
							error = " Parameter "+paramDescription.getName()+" should be of type LONG";
							break;
						}
					} 
				} 
			}
		}
		return error;
	}


}

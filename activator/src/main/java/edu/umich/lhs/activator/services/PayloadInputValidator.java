package edu.umich.lhs.activator.services;

import edu.umich.lhs.activator.domain.ParamDescription;
import edu.umich.lhs.activator.domain.PayloadProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * Class to check the validity of an api supplied input with the intended target payload
 * Created by grosscol on 2017-09-14.
 */
public class PayloadInputValidator {

  final PayloadProvider pp;
  final Map<String, Object> input;
  private List<String> messages;

  public PayloadInputValidator(final PayloadProvider provider, final Map<String, Object> inputData){
    pp = provider;
    input = inputData;
    messages = new ArrayList<>();
  }

  public boolean isValid(){
    boolean result = false;
    if(input == null){
      messages.add("No inputs given.");
    }
    else if(arityMatches() & paramNamesPresent() & paramTypesMatch() & paramValuesInRange()){
        result = true;
    }
    return result;
  }

  public List<String> messages(){
    return messages;
  }

  // Specific validity requirements

  boolean arityMatches(){
    boolean result =  pp.getNoOfParams() == input.size();
    if(result == false){
      messages.add("Number of input parameters should be "+pp.getNoOfParams());
    }
    return result;
  }

  boolean paramNamesPresent(){
    boolean result = true;
    for(ParamDescription param : pp.getParams()){
      if(!input.containsKey(param.getName())){
        result = false;
        messages.add("Input parameter "+param.getName()+" is missing.");
      }
    }
    return result;
  }

  /* This seems untennable as Java makes assumptions about number formats that
  other languages do not.  e.g. NA in the R language could be a valid input to an integer
  parameter.
   */
  boolean paramTypesMatch(){
    return true;
  }

  // Same problem as above.  There is not language agnostic way to get this correct.
  boolean paramValuesInRange(){
    return false;
  }

  public String getMessage() {
    if(messages.isEmpty()){
      return("");
    }
    return( StringUtils.join(messages, "\n  ") );
  }


}

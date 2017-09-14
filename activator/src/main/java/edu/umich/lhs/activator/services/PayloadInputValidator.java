package edu.umich.lhs.activator.services;

import edu.umich.lhs.activator.domain.PayloadProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class to check the validity of an api supplied input with the intended target payload
 * Created by grosscol on 2017-09-14.
 */
public class PayloadInputValidator {

  final PayloadProvider pp;
  final Map<String, Object> input;
  List<String> messages;

  public PayloadInputValidator(final PayloadProvider provider, final Map<String, Object> inputData){
    pp = provider;
    input = inputData;
    messages = new ArrayList<>();
  }

  public boolean isValid(){
    return arityMatches() && paramNamesPresent() && paramTypesMatch() && paramValuesInRange();
  }

  public List<String> messages(){
    return messages;
  }

  // Specific validity requirements

  boolean arityMatches(){
    return pp.getNoOfParams() == input.size();
  }

  boolean paramNamesPresent(){
    return false;
  }

  boolean paramTypesMatch(){
    return false;
  }

  boolean paramValuesInRange(){
    return false;
  }

}

package edu.umich.lhs.activator.services;

import edu.umich.lhs.activator.domain.PayloadProvider;

/**
 * Created by grosscol on 2017-09-11.
 */
public class PayloadProviderValidator {

  public static final String VALIDATED_MESSAGE = "payload provider valid.";
  public static final String ARITY_MESSAGE = "Incorrect number of parameters declared.";
  public static final String FUNC_NAME_MESSAGE = "Function does not have valid name.";
  public static final String CONTENT_MESSAGE = "Content is empty.";
  public static final String RETURN_TYPE_MESSAGE = "Invalid return type.";

  private final PayloadProvider provider;
  private String message = "";

  public PayloadProviderValidator(final PayloadProvider provider) {
    this.provider = provider;
  }

  public boolean verify(){
    message = "";
    boolean result = false;
    if( verifyInputArity() && verifyFunctionName() &&
        verifyPayloadContent() && verifyReturnType() ){
      message = VALIDATED_MESSAGE;
      result = true;
    }
    return result;
  }

  public boolean verifyInputArity(){
    if( provider.getNoOfParams() == provider.getParams().size()){
      return true;
    }
    message += ARITY_MESSAGE;
    message += " Expected: " + provider.getNoOfParams();
    message += " Found: " + provider.getParams().size();
    return false;
  }

  public boolean verifyFunctionName(){
    if(provider.getFunctionName() != null && !provider.getFunctionName().isEmpty()){
      return true;
    }
    message += FUNC_NAME_MESSAGE;
    return false;
  }

  public boolean verifyPayloadContent(){
    if(provider.getContent() != null && !provider.getContent().isEmpty()){
      return true;
    }

    message += CONTENT_MESSAGE;
    return false;
  }

  public boolean verifyReturnType(){
    if(provider.getReturnType() != null){
      return true;
    }
    message += RETURN_TYPE_MESSAGE;
    return false;
  }

  public String getMessage() {
    return message;
  }
}

package edu.umich.lhs.activator.services;

import edu.umich.lhs.activator.domain.PayloadProvider;

/**
 * Created by grosscol on 2017-09-11.
 */
public class PayloadProviderValidator {

  private final PayloadProvider provider;
  private String message;

  public PayloadProviderValidator(final PayloadProvider provider) {
    this.provider = provider;
  }

  public boolean verify(){
    message = "";
    return false;
  }

  public boolean verifyInputArity(){
    message += "Arity problem";
    return false;
  }

  public boolean verifyParameters(){
    message += "Parameter problem";
    return false;
  }

  public boolean verifyParameterLimits(){
    message += "Parameter Limit problem";
    return false;
  }

}

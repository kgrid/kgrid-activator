package edu.umich.lhs.activator.domain;

/**
 * Extracted from KnoweledgeObject by grosscol on 2017-09-11.
 */
public class Payload {

  private String content;

  private String engineType;

  private String functionName;

  public Payload() {
    content = "";
    engineType = "NONE";
    functionName = "";
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getEngineType() {
    return engineType.toUpperCase();
  }

  public void setEngineType(String engineType) {
    this.engineType = engineType;
  }

  public String getFunctionName() {
    return functionName;
  }

  public void setFunctionName(String functionName) {
    this.functionName = functionName;
  }

  @Override
  public String toString() {
    return "Payload [content=" + content + ", engineType=" + engineType + ", functionName="
        + functionName
        + "]";
  }


}

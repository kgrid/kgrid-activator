package edu.umich.lhs.activator.domain;

import edu.umich.lhs.activator.domain.KnowledgeObject.Payload;

/**
 * Created by nggittle on 3/27/2017.
 */
public class KnowledgeObjectBuilder {
  private KnowledgeObject ko;

  public KnowledgeObjectBuilder() {
    ko = new KnowledgeObject();
  }

  public KnowledgeObjectBuilder metadata(Metadata metadata) {
    ko.metadata = metadata;
    return this;
  }

  public KnowledgeObjectBuilder inputMessage(String inputMessage) {
    ko.inputMessage = inputMessage;
    return this;
  }

  public KnowledgeObjectBuilder outputMessage(String outputMessage) {
    ko.outputMessage = outputMessage;
    return this;
  }

  public KnowledgeObjectBuilder payload(Payload payload) {
    ko.payload = payload;
    return this;
  }

  public KnowledgeObjectBuilder url(String url) {
    ko.url = url;
    return this;
  }

  public KnowledgeObjectBuilder payloadContent (String content) {
    if(ko.payload != null) {
      ko.payload.content = content;
    } else {
      ko.payload = ko.genPayload(content, "", "");
    }
    return this;
  }

  public KnowledgeObjectBuilder payloadFunctionName (String functionName) {
    if(ko.payload != null) {
      ko.payload.functionName = functionName;
    } else {
      ko.payload = ko.genPayload("", "", functionName);
    }
    return this;
  }

  public KnowledgeObjectBuilder payloadEngineType (String engineType) {
    if(ko.payload != null) {
      ko.payload.engineType = engineType;
    } else {
      ko.payload = ko.genPayload("", engineType, "");
    }
    return this;
  }

  public KnowledgeObject build(){
    return ko;
  }


}
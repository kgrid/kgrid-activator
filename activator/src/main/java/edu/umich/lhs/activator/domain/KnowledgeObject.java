package edu.umich.lhs.activator.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


@JsonIgnoreProperties(ignoreUnknown = true)
public class KnowledgeObject {

	@JsonInclude(Include.NON_NULL)
	public Metadata metadata;

	public String inputMessage;

	public String outputMessage;

	public Payload payload;

	@JsonInclude(Include.NON_NULL)
	public String url;
	
	public KnowledgeObject() {
		// TODO Auto-generated constructor stub
	}

	public Payload genPayload(String content, String engineType, String functionName) {
		this.payload = new Payload();
		this.payload.setContent(content);
		this.payload.setEngineType(engineType);
		this.payload.setFunctionName(functionName);
		return this.payload;
	}
	
	@Override
	public String toString() {
		return "KnowledgeObject [inputMessage=" + inputMessage + ", outputMessage=" + outputMessage + ", payload="
				+ payload + ", url=" + url + "]";
	}


}

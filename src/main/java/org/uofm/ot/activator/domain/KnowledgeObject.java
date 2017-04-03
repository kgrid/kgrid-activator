package org.uofm.ot.activator.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class KnowledgeObject {

	public Metadata metadata;
	
	public String inputMessage; 
	
	public String outputMessage;
	
	public Payload payload;
	
	public String url;
	
	public KnowledgeObject() {
		// TODO Auto-generated constructor stub
	}

	public Payload genPayload(String content, String engineType, String functionName) {
		this.payload = new Payload();
		this.payload.content = content;
		this.payload.engineType = engineType;
		this.payload.functionName = functionName;
		return this.payload;
	}
	
	@Override
	public String toString() {
		return "KnowledgeObject [inputMessage=" + inputMessage + ", outputMessage=" + outputMessage + ", payload="
				+ payload + ", url=" + url + "]";
	}

	public class Payload {
		
		public String content;
		
		public String engineType;
		
		public String functionName;
		
		public Payload() {
			// TODO Auto-generated constructor stub
		}

		@Override
		public String toString() {
			return "Payload [content=" + content + ", engineType=" + engineType + ", functionName=" + functionName
					+ "]";
		}


	}
}

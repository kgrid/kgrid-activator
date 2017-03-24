package org.uofm.ot.executionStack.transferObjects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class KnowledgeObjectDTO {

	public Metadata metadata;
	
	public String inputMessage; 
	
	public String outputMessage;
	
	public Payload payload;
	
	public String url;
	
	public KnowledgeObjectDTO() {
		// TODO Auto-generated constructor stub
	}

	public void genPayload(String content, String engineType, String functionName) {
		this.payload = new Payload();
		this.payload.content = content;
		this.payload.engineType = engineType;
		this.payload.functionName = functionName;
	}
	
	@Override
	public String toString() {
		return "KnowledgeObjectDTO [inputMessage=" + inputMessage + ", outputMessage=" + outputMessage + ", payload="
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

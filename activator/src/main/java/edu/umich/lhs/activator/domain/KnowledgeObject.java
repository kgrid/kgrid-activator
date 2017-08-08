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
		
		private String content;
		
		private String engineType;
		
		private String functionName;

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

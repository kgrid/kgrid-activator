package org.uofm.ot.activator.adapter.gateway;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by grosscol on 2017-06-12.
 */
public class WebSockMessage {
  @JsonProperty
  public List buffers;
  @JsonProperty
  public String channel;
  @JsonProperty
  public WebSockContent content;
  @JsonProperty
  public WebSockHeader header;
  @JsonProperty
  public HashMap<String, Object> metadata;
  @JsonProperty("msg_id")
  public String messageId;
  @JsonProperty("msg_type")
  public String messageType;
  @JsonProperty("parent_header")
  public HashMap<String, Object> parentHeader;


  public WebSockMessage(){
    content = new WebSockContent();
  }

  @JsonIgnore
  public boolean isError(){
    return messageType.contentEquals("error");
  }

  @JsonIgnore
  public boolean isResult(){
    return messageType.contentEquals("stream");
  }

  static class WebSockMessageBuilder {

    static WebSockMessage buildPayloadRequest(String payload, String session_id) {
      WebSockMessage req = buildPayloadRequest(payload);
      req.header.setSession(session_id);
      return req;
    }

    static WebSockMessage buildUserExpRequest(Map expr, String session_id){
      WebSockMessage req = buildPayloadRequest("", session_id);
      req.content.userExpressions = expr;
      return req;
    }

    static WebSockMessage buildPayloadRequest(String payload) {
      WebSockMessage req = new WebSockMessage();
      req.header = new WebSockHeader();
      req.content = new WebSockContent();
      req.buffers = new ArrayList();
      req.parentHeader = new HashMap<>();

      req.header.setUsername("");
      req.header.setVersion("5.0");
      req.header.setMessageId(UUID.randomUUID().toString());
      req.header.setMessageType("execute_request");

      req.channel = "shell";
      req.content.code = payload;

      return req;
    }
  }
}

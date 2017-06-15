package org.uofm.ot.activator.adapter.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by grosscol on 2017-06-12.
 */
public class WebSockMessage {
  @JsonProperty
  public List buffers;
  @JsonProperty
  public String channel;
  @JsonProperty
  public HashMap<String, Object> content;
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

  static class WebSockMessageBuilder {

    static WebSockMessage buildPayloadRequest(String payload) {
      WebSockMessage req = new WebSockMessage();
      req.header = new WebSockHeader();
      req.content = new HashMap<String, Object>();

      req.header.setUsername("");
      req.header.setVersion("5.0");
      req.header.setSession("demoid");
      req.header.setMessageId("deadbeef");
      req.header.setMessageType("execute_request");

      req.channel = "shell";

      req.content.put("code", payload);
      req.content.put("silent", false);
      req.content.put("store_history", false);
      req.content.put("user_expressions", new HashMap<>());
      req.content.put("allow_stdin", false);

      req.content.put("metadata", new HashMap<>());
      req.content.put("buffers", new ArrayList<>());

      return req;
    }
  }
}

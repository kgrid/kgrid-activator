package org.uofm.ot.activator.adapter.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by grosscol on 2017-06-12.
 */
public class WebSockMessage {
  @JsonProperty
  HashMap<String, Object> header;
  @JsonProperty("parent_header")
  HashMap<String, Object> parentHeader;
  @JsonProperty
  String channel;
  @JsonProperty
  HashMap<String, Object> content;
  @JsonProperty
  HashMap<String, Object> metadata;
  @JsonProperty
  List buffers;

  /**
   * Created by grosscol on $-$-$.
   */
  static class WebSockMessageBuilder {

    static WebSockMessage buildPayloadRequest(String payload) {
      WebSockMessage req = new WebSockMessage();
      req.header = new HashMap<String, Object>();
      req.content = new HashMap<String, Object>();

      req.header.put("username", "");
      req.header.put("version", "5.0");
      req.header.put("session", "demoid");
      req.header.put("msg_id", "deadbeef");
      req.header.put("msg_type", "execute_request");

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

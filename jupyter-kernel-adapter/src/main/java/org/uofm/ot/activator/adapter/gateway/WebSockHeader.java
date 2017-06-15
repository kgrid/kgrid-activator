package org.uofm.ot.activator.adapter.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data mapping class for the header of the websockets requests.
 * Used for tracking conversations via parent_header of WebSockMessage
 *
 * Created by grosscol on 2017-06-13.
 */
public class WebSockHeader {
  @JsonProperty
  private
  String username;
  @JsonProperty
  private
  String version;
  @JsonProperty
  private
  String session;
  @JsonProperty("msg_id")
  private
  String messageId;
  @JsonProperty("msg_type")
  private
  String messageType;
  @JsonProperty
  private
  String date;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getSession() {
    return session;
  }

  public void setSession(String session) {
    this.session = session;
  }

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  public String getMessageType() {
    return messageType;
  }

  public void setMessageType(String messageType) {
    this.messageType = messageType;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }
}

package org.uofm.ot.activator.adapter.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Class for mapping the components of the /api/kernels WEB SOCKET responses from a Jupyter Kernel Gateway
 * Created by grosscol on 2017-06-12.
 */
public class WebSockResponse {
  @JsonProperty
  private
  Map header;

  @JsonProperty("parent_header")
  private
  Map parentHeader;

  @JsonProperty
  private
  Map metadata;

  @JsonProperty
  private
  Map content;

  @JsonProperty
  private
  List buffers;

  public Map getHeader() {
    return header;
  }

  public void setHeader(Map header) {
    this.header = header;
  }

  public Map getParentHeader() {
    return parentHeader;
  }

  public void setParentHeader(Map parentHeader) {
    this.parentHeader = parentHeader;
  }

  public Map getMetadata() {
    return metadata;
  }

  public void setMetadata(Map metadata) {
    this.metadata = metadata;
  }

  public Map getContent() {
    return content;
  }

  public void setContent(Map content) {
    this.content = content;
  }

  public List getBuffers() {
    return buffers;
  }

  public void setBuffers(List buffers) {
    this.buffers = buffers;
  }
}

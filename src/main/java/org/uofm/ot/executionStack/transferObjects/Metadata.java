package org.uofm.ot.executionStack.transferObjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;

/**
 * Created by nggittle on 3/24/2017.
 */

public class Metadata {

  @JsonInclude(Include.NON_EMPTY)
  private String title;

  @JsonInclude(Include.NON_EMPTY)
  private String description;

  @JsonInclude(Include.NON_EMPTY)
  private List<Citation> citations;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<Citation> getCitations() {
    return citations;
  }

  public void setCitations(List<Citation> citations) {
    this.citations = citations;
  }

}

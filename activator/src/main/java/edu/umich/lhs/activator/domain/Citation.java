package edu.umich.lhs.activator.domain;

/**
 * Created by nggittle on 3/24/2017.
 */
public class Citation {

  private String citation_id;

  private String citation_title;

  private String citation_at;

  public String getCitation_id() {
    return citation_id;
  }

  public void setCitation_id(String citation_id) {
    this.citation_id = citation_id;
  }

  public String getCitation_title() {
    return citation_title;
  }

  public void setCitation_title(String citation_title) {
    this.citation_title = citation_title;
  }

  public String getCitation_at() {
    return citation_at;
  }

  public void setCitation_at(String citation_at) {
    this.citation_at = citation_at;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Citation citation = (Citation) o;

    if (citation_title != null ? !citation_title
        .equals(citation.citation_title) : citation.citation_title != null) {
      return false;
    }
    return citation_at != null ? citation_at.equals(citation.citation_at) : citation.citation_at == null;
  }

  @Override
  public int hashCode() {
    int result = citation_title != null ? citation_title.hashCode() : 0;
    result = 31 * result + (citation_at != null ? citation_at.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Citation{" +
        "citation_title='" + citation_title + '\'' +
        ", citation_at='" + citation_at + '\'' +
        '}';
  }
}

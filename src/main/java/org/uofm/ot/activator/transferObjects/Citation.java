package org.uofm.ot.activator.transferObjects;

/**
 * Created by nggittle on 3/24/2017.
 */
public class Citation {

  private String title;

  private String at;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getAt() {
    return at;
  }

  public void setAt(String at) {
    this.at = at;
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

    if (title != null ? !title.equals(citation.title) : citation.title != null) {
      return false;
    }
    return at != null ? at.equals(citation.at) : citation.at == null;
  }

  @Override
  public int hashCode() {
    int result = title != null ? title.hashCode() : 0;
    result = 31 * result + (at != null ? at.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Citation{" +
        "title='" + title + '\'' +
        ", at='" + at + '\'' +
        '}';
  }
}

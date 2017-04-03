package org.uofm.ot.activator.domain;


/**
 * A wrapper so that errors can be serialized and returned in correct json format
 * Created by nggittle on 3/29/17.
 */
public class ErrorInfo {

  private String ex;

  private String exMessage;

  private String url;

  public ErrorInfo(String ex, String exMessage, String url) {
    this.ex = ex;
    this.exMessage = exMessage;
    this.url = url;
  }
}

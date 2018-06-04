package org.kgrid.activator;

import java.util.HashMap;
import java.util.Map;

public class EndPointResult<T> {

  private final T result;
  private final Map<String, Object> info;

  public EndPointResult(T result, Map<String, Object> info) {
    this.result = result;
    this.info = info;
  }

  public EndPointResult(T result) {
    this.result = result;
    info = new HashMap<String, Object>();
  }

  public T getResult() {
    return result;
  }

  public Map<String, Object> getInfo() {
    return info;
  }

}

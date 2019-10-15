package org.kgrid.activator.services;

import javax.validation.constraints.NotNull;
import org.kgrid.shelf.domain.ArkId;

public class EndpointId implements Comparable {

  private ArkId arkId;
  private String endpointName;

  public EndpointId(ArkId arkId, String endpointName){
    this.arkId = arkId;
    if(endpointName.startsWith("/")) {
      this.endpointName = endpointName;
    } else {
      this.endpointName = "/" + endpointName;
    }
  }

  public EndpointId(String naan, String name, String version, String endpointName) {
    this.arkId = new ArkId(naan, name, version);
    if(endpointName.startsWith("/")) {
      this.endpointName = endpointName;
    } else {
      this.endpointName = "/" + endpointName;
    }
  }

  public void setArkId(ArkId arkId) {
    this.arkId = arkId;
  }

  public void setEndpointName(String endpointName) {
    this.endpointName = endpointName;
  }

  public ArkId getArkId() {
    return arkId;
  }

  public String getEndpointName() {
    return endpointName;
  }

  @Override
  public String toString() {
    return arkId.getSlashArkImplementation() +  endpointName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    EndpointId that = (EndpointId) o;

    if (arkId != null ? !arkId.equals(that.arkId) : that.arkId != null) {
      return false;
    }

    // Custom matching if one ark is missing a version:
    if(arkId.getImplementation() == null || that.arkId.getImplementation() == null){
       if(arkId.getNaan() == that.getArkId().getNaan() && arkId.getName() == that.getArkId().getName()){
         return endpointName != null ? endpointName.equals(that.endpointName)
             : that.endpointName == null;
       }
    }

    return endpointName != null ? endpointName.equals(that.endpointName)
        : that.endpointName == null;
  }

  @Override
  public int hashCode() {
    int result = arkId != null ? arkId.hashCode() : 0;
    result = 31 * result + (endpointName != null ? endpointName.hashCode() : 0);
    return result;
  }

  @Override
  public int compareTo(@NotNull Object o) {
    if (this == o) {
      return 0;
    }

    EndpointId that = (EndpointId)o;

    if(this.getArkId().getNaan().compareTo(that.getArkId().getNaan()) != 0) {
      return this.getArkId().getNaan().compareTo(that.getArkId().getNaan());
    }

    if(this.getArkId().getName().compareTo(that.getArkId().getName()) != 0) {
      return this.getArkId().getName().compareTo(that.getArkId().getName());
    }

    if(this.getArkId().getImplementation() != null && that.getArkId().getImplementation() != null){
      if(this.getArkId().getImplementation().compareTo(that.getArkId().getImplementation()) != 0) {
        return this.getArkId().getImplementation().compareTo(that.getArkId().getImplementation());
      }
    }

    return this.getEndpointName().compareTo(that.endpointName);
  }
}

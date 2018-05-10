package edu.umich.lhs.activator.services;

import edu.umich.lhs.activator.domain.ArkId;
import edu.umich.lhs.activator.domain.Result;
import edu.umich.lhs.activator.repository.Shelf;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

@Component
public class SmoketestHealthIndicator extends AbstractHealthIndicator {

  private final Shelf shelf;
  private final HeritageActivationService activationService;

  @Autowired
  public SmoketestHealthIndicator(Shelf shelf, HeritageActivationService activationService) {
    this.shelf = shelf;
    this.activationService = activationService;
  }

  /**
   * Tests the two built-in javascript objects with sample inputs
   * @param builder
   */

  @Override
  protected void doHealthCheck(Health.Builder builder) {

    builder
        .withDetail("Does Hello World work?", testHelloWorldObject() ? "Yes" : "No")
        .withDetail("Does Prescription Counter work?", testPrescriptionCounter() ? "Yes" : "No")
        .up();
  }


  private boolean testHelloWorldObject() {
    Map<String, Object> inputs = new HashMap<>();
    inputs.put("name", "World");
    ArkId helloWorldArk = new ArkId("ark:/hello/world");
    Result result = activationService.validateAndExecute(inputs, shelf.getObject(helloWorldArk));

    return ("Hello, World".equals(result.getResult()));
  }

  private boolean testPrescriptionCounter() {
    Map<String, Object> inputs = new HashMap<>();
    inputs.put("DrugIDs", "1 2 3 4");
    ArkId counterArk = new ArkId("ark:/prescription/counter");
    Result result = activationService.validateAndExecute(inputs, shelf.getObject(counterArk));

    return new Integer(4).equals(result.getResult());
  }
}

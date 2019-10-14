package org.kgrid.activator.services;

import static org.junit.Assert.assertEquals;
import static org.kgrid.activator.utils.RepoUtils.C_D_F;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.kgrid.activator.ActivatorException;
import org.kgrid.activator.EndPointResult;
import org.kgrid.adapter.api.Executor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class EndpointExecutionTests {

  public static final EndpointId C_D_F_WELCOME = new EndpointId(C_D_F, "welcome");


  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  ActivationService activationService;


  @Test
  public void activateCreatesEndpointResultWithKoResultAndInfoHasInput() {

    Executor executor = mock(Executor.class);

    Map<EndpointId, Endpoint> endpointMap = new  HashMap<>();
    final Endpoint endpoint = Endpoint.Builder
        .anEndpoint()
        .withExecutor(executor)
        .build();
    endpointMap.put(C_D_F_WELCOME, endpoint);

    activationService = new ActivationService(null, endpointMap);

    given(executor.execute(any()))
        .willReturn("Welcome to Knowledge Grid, Bob");


    String inputs = "bar";

    // when
    EndPointResult result = activationService.execute(C_D_F_WELCOME, inputs);

    assertEquals("Welcome to Knowledge Grid, Bob", result.getResult());

    assertEquals(inputs, result.getInfo().get("inputs"));
  }

  @Test
  public void executeThrowsMissingExecutorException() {
    expectedException.expect(ActivatorException.class);
    expectedException.expectMessage("Executor not found");

    Map<EndpointId, Endpoint> endpointMap = new  HashMap<>();
    final Endpoint endpoint = Endpoint.Builder
        .anEndpoint()
        .withDeployment(null)
        .build();
    endpointMap.put(C_D_F_WELCOME, endpoint);

    activationService = new ActivationService(null, endpointMap);

    // when
    EndPointResult result = activationService.execute(C_D_F_WELCOME, "");
  }
}

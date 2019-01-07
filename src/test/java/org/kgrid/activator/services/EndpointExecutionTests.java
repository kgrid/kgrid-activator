package org.kgrid.activator.services;

import static org.junit.Assert.assertEquals;
import static org.kgrid.activator.utils.RepoUtils.C_D_F;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.kgrid.activator.ActivatorException;
import org.kgrid.activator.EndPointResult;
import org.kgrid.adapter.api.Executor;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class EndpointExecutionTests {

  public static final String C_D_F_WELCOME = C_D_F.getDashArkImplementation() + "/welcome";
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @InjectMocks
  ActivationService activationService;

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void activateCreatesEndpointResultWithKoResultAndInfoHasInput() {

    Executor executor = mock(Executor.class);

    given(executor.execute(any()))
        .willReturn("Welcome to Knowledge Grid, Bob");

    final Endpoint endpoint = Endpoint.Builder
        .anEndpoint()
        .withExecutor(executor)
        .build();

    activationService.getEndpoints().put(C_D_F_WELCOME, endpoint);

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

    final Endpoint endpoint = Endpoint.Builder
        .anEndpoint()
        .withDeployment(null)
        .build();

    activationService.getEndpoints().put(C_D_F_WELCOME, endpoint);

    // when
    EndPointResult result = activationService.execute(C_D_F_WELCOME, "");
  }

}
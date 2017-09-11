package edu.umich.lhs.activator.services;

import static org.junit.Assert.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.umich.lhs.activator.domain.Kobject;
import edu.umich.lhs.activator.domain.Payload;
import edu.umich.lhs.activator.domain.PayloadProvider;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


/**
 * Created by grosscol on 2017-09-11.
 */
public class PayloadProviderValidatorTest {

  PayloadProvider pp = mock(PayloadProvider.class);
  PayloadProviderValidator validator;

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    validator = new PayloadProviderValidator(pp);
  }

  @Test
  public void validatesZeroParams() throws Exception {
    when(pp.getNoOfParams()).thenReturn(0);
    when(pp.getParams()).thenReturn(new ArrayList<>());

    boolean result = validator.verifyInputArity();
    assertThat(result, equalTo(true));
  }

  @Test
  public void messageForBadArity() throws Exception {
  }

  @Test
  public void verifyParameters() throws Exception {
  }

  @Test
  public void verifyParameterLimits() throws Exception {
  }

}
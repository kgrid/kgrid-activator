package edu.umich.lhs.activator.services;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.umich.lhs.activator.domain.DataType;
import edu.umich.lhs.activator.domain.ParamDescription;
import edu.umich.lhs.activator.domain.PayloadProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


/**
 * Created by grosscol on 2017-09-11.
 */
public class PayloadProviderValidatorTest {

  // Source dummy parameters
  private final ParamDescription foo = new ParamDescription("foo", DataType.STRING, null, null);
  private final ParamDescription bar = new ParamDescription("bar", DataType.INT, -5, 20);
  @Rule
  public ExpectedException expectedEx = ExpectedException.none();
  private PayloadProvider pp = mock(PayloadProvider.class);
  private PayloadProviderValidator validator;

  @Before
  public void setUp() throws Exception {
    validator = new PayloadProviderValidator(pp);
  }

  // Informational Messages
  @Test
  public void noMessageBeforeValidation() throws Exception {
    when(pp.getNoOfParams()).thenReturn(2);
    when(pp.getParams()).thenReturn(new ArrayList<>());

    String msg = validator.getMessage();
    assertThat(msg, equalTo(""));
  }

  @Test
  public void successfulValidationMessage() throws Exception {
    when(pp.getNoOfParams()).thenReturn(2);
    ArrayList<ParamDescription> params = new ArrayList<>(Arrays.asList(foo, bar));
    when(pp.getParams()).thenReturn(params);
    when(pp.getFunctionName()).thenReturn("myFun");
    when(pp.getContent()).thenReturn("dummy Content");
    when(pp.getEngineType()).thenReturn("dummy engine");
    when(pp.getReturnType()).thenReturn(String.class);

    validator.verify();
    String msg = validator.getMessage();
    assertThat(msg, equalTo(PayloadProviderValidator.VALIDATED_MESSAGE));
  }

  // Parameter Arity
  @Test
  public void validatesZeroParams() throws Exception {
    when(pp.getNoOfParams()).thenReturn(0);
    when(pp.getParams()).thenReturn(new ArrayList<>());

    boolean result = validator.verifyInputArity();
    assertThat(result, equalTo(true));
  }

  @Test
  public void validatesParamCount() throws Exception {
    when(pp.getNoOfParams()).thenReturn(2);

    ArrayList<ParamDescription> params = new ArrayList<>(Arrays.asList(foo, bar));
    when(pp.getParams()).thenReturn(params);

    boolean result = validator.verifyInputArity();
    assertThat(result, equalTo(true));
  }

  @Test
  public void catchFewerParams() throws Exception {
    when(pp.getNoOfParams()).thenReturn(2);
    ArrayList<ParamDescription> params = new ArrayList<>(Collections.singletonList(foo));
    when(pp.getParams()).thenReturn(params);

    boolean result = validator.verifyInputArity();
    assertThat(result, equalTo(false));
  }

  @Test
  public void catchMoreParams() throws Exception {
    when(pp.getNoOfParams()).thenReturn(0);
    ArrayList<ParamDescription> params = new ArrayList<>(Collections.singletonList(foo));
    when(pp.getParams()).thenReturn(params);

    boolean result = validator.verifyInputArity();
    assertThat(result, equalTo(false));
  }

  @Test
  public void messageForBadArity() throws Exception {
    when(pp.getNoOfParams()).thenReturn(2);
    ArrayList<ParamDescription> params = new ArrayList<>(Collections.singletonList(foo));
    when(pp.getParams()).thenReturn(params);

    validator.verifyInputArity();
    String msg = validator.getMessage();
    assertThat(msg, containsString(PayloadProviderValidator.ARITY_MESSAGE));
  }

  // Function Name Check
  @Test
  public void blankName() throws Exception {
    when(pp.getFunctionName()).thenReturn("");

    boolean result = validator.verifyFunctionName();
    assertThat(result, equalTo(false));
  }

  @Test
  public void nullName() throws Exception {
    when(pp.getFunctionName()).thenReturn(null);

    boolean result = validator.verifyFunctionName();
    assertThat(result, equalTo(false));
  }

  @Test
  public void nameMessage() throws Exception {
    when(pp.getFunctionName()).thenReturn(null);

    validator.verifyFunctionName();
    String msg = validator.getMessage();
    assertThat(msg, equalTo(PayloadProviderValidator.FUNC_NAME_MESSAGE));
  }

  // Payload Content Check
  @Test
  public void blankContent() throws Exception {
    when(pp.getContent()).thenReturn("");

    boolean result = validator.verifyPayloadContent();
    assertThat(result, equalTo(false));
  }

  @Test
  public void nullContent() throws Exception {
    when(pp.getContent()).thenReturn(null);

    boolean result = validator.verifyPayloadContent();
    assertThat(result, equalTo(false));
  }

  @Test
  public void contentMessage() throws Exception {
    when(pp.getFunctionName()).thenReturn(null);

    validator.verifyPayloadContent();
    String msg = validator.getMessage();
    assertThat(msg, equalTo(PayloadProviderValidator.CONTENT_MESSAGE));
  }

  // Return Type Check
  @Test
  public void nullReturnType() throws Exception {
    when(pp.getReturnType()).thenReturn(null);

    boolean result = validator.verifyReturnType();
    assertThat(result, equalTo(false));
  }

}
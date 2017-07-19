package org.uofm.ot.activator.adapter.gateway;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.LinkedHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Created by grosscol on 2017-07-18.
 */
public class SockResponseProcessorTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();
  private ArrayBlockingQueue<WebSockMessage> messageQ;
  private SockResponseProcessor responseProcessor;
  private long testDuration = 4_000_000L;

  //TODO: mock the messages instead of building real ones
  private long testPollInterval = 1_000_000L;

  private WebSockMessage buildResultMessage() {
    WebSockMessage msg = new WebSockMessage();
    msg.messageType = "execute_result";

    LinkedHashMap<String, Object> result = new LinkedHashMap<>();
    result.put("value", "foo");
    msg.content.data = new LinkedHashMap<>();
    msg.content.data.put("application/json", result);

    return msg;
  }

  private WebSockMessage buildErrorMessage() {
    WebSockMessage msg = new WebSockMessage();
    msg.messageType = "error";
    msg.content.ename = "SomeError";
    msg.content.evalue = "An error description";

    return msg;
  }

  @Before
  public void setup() {
    messageQ = new ArrayBlockingQueue<>(5);
    responseProcessor = new SockResponseProcessor(messageQ);
  }

  @Test
  public void queueEmpty() throws Exception {
    responseProcessor.beginProcessing(10_000, 2_000);
    assertThat(responseProcessor.encounteredTimeout(), equalTo(true));
  }

  @Test
  public void noResultNoError() throws Exception {
    messageQ.add(new WebSockMessage());

    responseProcessor.beginProcessing(10_000, 2_000);
    assertThat(responseProcessor.encounteredTimeout(), equalTo(true));
  }

  @Test
  public void hasResultMessage() throws Exception {
    messageQ.add(new WebSockMessage());
    messageQ.add(new WebSockMessage());
    messageQ.add(buildResultMessage());

    responseProcessor.beginProcessing(testDuration, testPollInterval);
    assertThat(responseProcessor.encounteredResult(), equalTo(true));
  }

  @Test
  public void hasErrorMessage() throws Exception {
    messageQ.put(new WebSockMessage());
    messageQ.add(new WebSockMessage());
    messageQ.put(buildErrorMessage());

    responseProcessor.beginProcessing(testDuration, testPollInterval);
    assertThat(responseProcessor.encounteredError(), equalTo(true));
  }

  @Test
  public void tooLargePollInterval() throws Exception {
    messageQ.add(buildResultMessage());

    responseProcessor.beginProcessing(testDuration, testDuration + 1000);
    assertThat(responseProcessor.encounteredResult(), equalTo(true));
  }
}

package org.uofm.ot.activator.adapter.gateway;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import javax.websocket.DeploymentException;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.uofm.ot.activator.adapter.TestUtils;
import org.uofm.ot.activator.exception.OTExecutionStackException;

public class SockPuppetTest {

  @Mock
  private Session session = mock(Session.class);

  @Mock
  private RemoteEndpoint.Async remoteEndpoint = mock(RemoteEndpoint.Async.class);

  @Mock
  public WebSocketContainer container = mock(WebSocketContainer.class);

  @InjectMocks
  private SockPuppet wsClient;

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private final String hello_code = "print('Hello, Test')";

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void unableToConnect() throws Exception {
    URI testUri = URI.create("ws://not.reachable.host:9999");
    when(container.connectToServer(wsClient, testUri))
        .thenThrow(new DeploymentException("test exception"));

    exception.expect(OTExecutionStackException.class);
    exception.expectMessage("Unable to connect to Jupyter Gateway");
    exception.expectCause(Matchers.instanceOf(DeploymentException.class));

    wsClient.connectToServer(testUri);
  }

  @Test
  public void sendPayloadReturnsHeader() throws Exception {
    when(session.getAsyncRemote()).thenReturn(remoteEndpoint);
    when(session.isOpen()).thenReturn(true);
    WebSockHeader header = wsClient.sendPayload(hello_code);

    assertThat(header.getMessageId(), not(isEmptyOrNullString()));
  }

  @Test
  public void sendingClosedSession() throws Exception {
    when(session.isOpen()).thenReturn(false);

    exception.expect(OTExecutionStackException.class);
    exception.expectMessage("session not open");

    wsClient.sendPayload(hello_code);
  }

  @Test
  public void sendingNoSession() throws Exception {
    wsClient.setSession(null);

    exception.expect(OTExecutionStackException.class);
    exception.expectMessage("no session");

    wsClient.sendPayload(hello_code);
  }

  @Test
  public void receivedMessagesAddedToQueue() throws Exception {
    String response = TestUtils.jsonFixture("socket-resp-exec-reply");
    for (int i = 0; i < 5; i++) {
      wsClient.onMessage(response);
    }

    assertThat(wsClient.getMessageQ(), hasSize(5));
  }

}

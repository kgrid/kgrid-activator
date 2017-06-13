package org.uofm.ot.activator.adapter.gateway;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import java.io.IOException;
import java.util.Collections;
import java.util.Scanner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

@RunWith(HierarchicalContextRunner.class)
public class SockPuppetTest {

  public String jsonFixture(String fixtureName) throws IOException {
    String json = new Scanner(
        SockPuppetTest.class.getResourceAsStream("/fixtures/" + fixtureName + ".json"), "UTF-8")
        .useDelimiter("\\A").next();
    return json;
  }

  @Before
  public void setup() {
  }

  // send payload returns message header
  @Test
  @Ignore
  public void sendPayloadReturnsHeader() throws Exception {

  }

  @Test
  @Ignore
  public void socketMsg() throws Exception {
    String kernel_id = "somekernel";

    // Spring Boot
    WebSocketTransport transport = new WebSocketTransport(new StandardWebSocketClient());
    WebSocketClient wsclient = new SockJsClient(Collections.singletonList(transport));

  }

}

package org.uofm.ot.activator.adapter;

import java.net.URI;
import org.junit.Test;

public class SockPuppetTest {

  private SockPuppet client;

  @Test
  public void connectTest() throws Exception {
    // Get start a new python kernel running
    String kernel_id = "foo";
    client = new SockPuppet(new URI("ws://localhost:8888/"+kernel_id+"/channels"));

  }
}

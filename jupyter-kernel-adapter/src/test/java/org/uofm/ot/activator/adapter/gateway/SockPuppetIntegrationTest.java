package org.uofm.ot.activator.adapter.gateway;

import java.net.URI;
import org.junit.Ignore;
import org.junit.Test;
import org.uofm.ot.activator.adapter.TestUtils;

/**
 * Created by grosscol on 2017-06-16.
 */
public class SockPuppetIntegrationTest {

  // Convenience method to start session.  Remove after mocking comms.
  public SockPuppet putOnSocks(String kernelId){
    SockPuppet socks = new SockPuppet();
    String dest = "ws://localhost:8888/api/kernels/" + kernelId + "/channels";
    socks.connectToServer(URI.create(dest));
    return socks;
  }

  private KernelMetadata kernel;

  @Test
  @Ignore
  public void sendMessage() throws Exception {
    SockPuppet socks = putOnSocks(kernel.getId());

    String msg = TestUtils.jsonFixture("socket-request");
    socks.sendText(msg);

    Thread.sleep(1000);

    socks.close();
  }

}

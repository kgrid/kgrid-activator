package org.uofm.ot.activator.adapter.gateway;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

/**
 * Class to encapsulate the I/O to the Jupyter Kernel Gateway API.
 * Includes both REST and WebSocket operations.
 */
@ClientEndpoint
public class SockPuppet {

  private Session sess = null;

  @OnOpen
  public void onOpen(final Session sess) {
    System.out.println("Socket Session Opened");
    this.sess = sess;
  }

  @OnClose
  public void onClose(final Session sess, final CloseReason rsn) {
    System.out.println("Socket Session Closed");
  }

  @OnMessage
  public void onMessage(final String message) {
    System.out.println("Message Received.");
    System.out.println(message);
  }

  public static void sendMessage(final String message, final Session sess) {
    sess.getAsyncRemote().sendText(message);
  }

  /**
   * Send a payload to the Jupyter KernelGateway for execution on a given kernel.
   *
   * @param payload code to be executed
   * @param sess web sockets session
   * @param kernel_id id of running jupytr kernel
   */
  public static void sendPayload(final String payload, final Session sess, final String kernel_id)
      throws UnsupportedEncodingException {

    String encoded_kernel_id = URLEncoder.encode(kernel_id, "UTF-8");

    // javax websocket implementation
    WebSocketContainer container =
        ContainerProvider.getWebSocketContainer();
    // connect
    String uri = "ws://localhost:8888/api/kernels/" + encoded_kernel_id + "/channels";
    //Session sess = null;
    //try {
    //  sess = container.connectToServer(SockPuppet.class, URI.create(uri));
    //} catch (DeploymentException e) {
    //  e.printStackTrace();
    //} catch (IOException e) {
    //  e.printStackTrace();
    //}

    // send message
    //String msg = jsonFixture("socket-request");
    String msg = "Foo";
    SockPuppet.sendMessage(msg, sess);

    // wait for response
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }


}

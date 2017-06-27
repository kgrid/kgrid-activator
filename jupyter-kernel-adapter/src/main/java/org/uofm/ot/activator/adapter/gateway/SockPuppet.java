package org.uofm.ot.activator.adapter.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import org.uofm.ot.activator.adapter.gateway.WebSockMessage.WebSockMessageBuilder;
import org.uofm.ot.activator.exception.OTExecutionStackException;

/**
 * Class to encapsulate the I/O to the Websocket Jupyter Kernel Gateway API endpoints.
 */
@ClientEndpoint
public class SockPuppet {

  private Session session = null;
  private ObjectMapper messageMapper;
  private WebSocketContainer container;
  private ArrayBlockingQueue<WebSockMessage> messageQ;

  public SockPuppet() {
    messageQ = new ArrayBlockingQueue<>(20);
    messageMapper = new ObjectMapper();
  }

  /**
   * Attempt to open websocket connection a url
   */
  public void connectToServer(URI uri) throws OTExecutionStackException {
    try {
      session = getContainer().connectToServer(this, uri);
    } catch (DeploymentException | IOException ex) {
      throw new OTExecutionStackException("Unable to connect to Jupyter Gateway: " + uri.toString(),
          ex);
    }
  }

  /**
   * Send a payload to the Jupyter KernelGateway for execution.
   *
   * @param payload code to be executed
   */
  public WebSockHeader sendPayload(String payload){
    return sendPayload(payload, "");

  }

  public WebSockHeader sendPayload(String payload, String sesion_id)
      throws OTExecutionStackException {

    // Build execution message using payload
    WebSockMessage msg = WebSockMessageBuilder.buildPayloadRequest(payload, sesion_id);
    sendJsonMessage(msg);
    return msg.header;
  }

  public WebSockHeader sendUserExpression(Map expr, String session_id){
    WebSockMessage msg = WebSockMessageBuilder.buildUserExpRequest(expr, session_id);
    sendJsonMessage(msg);
    return msg.header;
  }

  @OnOpen
  public void onOpen(final Session sess) {
    System.out.println("Socket Session Opened");
    this.session = sess;
  }

  @OnClose
  public void onClose(final Session sess, final CloseReason rsn) {
    System.out.println("Socket Session Closed");
  }

  @OnMessage
  public void onMessage(final String message) {
    System.out.println("\nMessage Received.");
    System.out.println(message);

    // Convert message to WebSockMessage
    WebSockMessage wsMessage = null;
    try {
      wsMessage = messageMapper.readValue(message, WebSockMessage.class);
    } catch (IOException e) {
      // TODO: log message unavailable
      e.printStackTrace();
    }

    // Add message to thread safe queue
    try {
      messageQ.offer(wsMessage, 2, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      // TODO: log waiting to put message in queue interrupted
      e.printStackTrace();
    }
  }

  public void sendText(final String message) throws OTExecutionStackException {
    if (session == null) {
      throw new OTExecutionStackException("Sending Error: no session.");
    } else if (session.isOpen() == false) {
      throw new OTExecutionStackException("Sending Error: session not open.");
    }
    //TODO: Info level log message sent
    //TODO: Debug level log message content
    System.out.println("\n== Sending Message ==");
    System.out.print(message);
    getSession().getAsyncRemote().sendText(message);
  }

  public void sendJsonMessage(final WebSockMessage msg) throws OTExecutionStackException {
    ObjectMapper mapper = new ObjectMapper();
    String json_msg = "";

    try {
      json_msg = mapper.writeValueAsString(msg);
    } catch (JsonProcessingException e) {
      throw new OTExecutionStackException(" Error while encoding payload to JSON. ");
    }

    sendText(json_msg);
  }

  @Override
  public void finalize() {
    this.close();
  }

  public void close() {
    if (getSession() != null && getSession().isOpen()) {
      try {
        getSession().close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public Session getSession() {
    return session;
  }

  public void setSession(Session session) {
    this.session = session;
  }

  public WebSocketContainer getContainer() {
    if (container == null) {
      setContainer(ContainerProvider.getWebSocketContainer());
    }
    return container;
  }

  public void setContainer(WebSocketContainer container) {
    this.container = container;
  }

  public ArrayBlockingQueue<WebSockMessage> getMessageQ() {
    return messageQ;
  }
}

package org.uofm.ot.activator.adapter;

import java.io.IOException;
import java.net.URI;
 
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;

//import javax.websocket.WebSocketContainer;
//import javax.websocket.ContainerProvider;

import org.json.JSONObject;
import com.mashape.unirest.http.*;
import com.mashape.unirest.http.exceptions.UnirestException;

@ClientEndpoint
public class SockPuppet{
  private Session sess = null;
  
  public SockPuppet(final URI endpointURI) {

	}

  @OnOpen
  public void onOpen(final Session sess) {

  }
  
  @OnClose
  public void onClose(final Session sess, final CloseReason rsn) {

  }
	@OnMessage
	public void onMessage(final String message) {

	}
 
	public void sendMessage(final String message) {
		sess.getAsyncRemote().sendText(message);
	}

  public static String startKernel(){
    try{
      HttpResponse<JsonNode> jsonResponse = Unirest.post("http://localhost:8888/post")
        .basicAuth("fakeuser","fakepass")
        .header("accept", "application/json")
        .field("name", "python")
        .asJson();

      JSONObject jobj = jsonResponse.getBody().getObject();
      Unirest.shutdown();

      return jobj.getString("id");
    } catch (Exception myEx) {
      return "";
    }   
  }
}

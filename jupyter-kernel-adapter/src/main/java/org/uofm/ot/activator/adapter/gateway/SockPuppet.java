package org.uofm.ot.activator.adapter.gateway;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.websocket.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@ClientEndpoint
public class SockPuppet {

  private Session sess = null;
  private URI baseURI;
  public RestTemplate restTemplate;

  public SockPuppet() throws URISyntaxException {
    this(new URI("http://localhost:8888"));
  }

  public SockPuppet(final URI baseURI) {
    this.restTemplate = new RestTemplate();
    this.baseURI = baseURI;
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

  public String gatewayVersion() {
    HashMap<String, Object> jb = new HashMap<>();
    URI targetURI = baseURI.resolve("/api");
    try {
      jb = restTemplate.getForObject(targetURI, HashMap.class);
    } catch (ResourceAccessException e){
      System.out.println(e.getMessage());
    }
    return (String) jb.get("version");
  }

  public List<KernelsResponse> getKernels() {
    List<KernelsResponse> list = new ArrayList<>();
    try {
      URI targetUri = new URI("http://localhost:8888/api/kernels");
      list = restTemplate.getForObject(targetUri, ArrayList.class);
    } catch (URISyntaxException | HttpClientErrorException | ResourceAccessException e) {
      System.out.println(e.getMessage());
      return list;
    }

    return list;
  }

  public String startKernel() {
    KernelsResponse resp;
    try {
      URI targetUri = new URI("http://localhost:8888/api/kernels");
      String request = "{\"name\": \"python\"}";
      resp = restTemplate.postForObject(targetUri, request, KernelsResponse.class);
    } catch (URISyntaxException | HttpClientErrorException | ResourceAccessException e) {
      System.out.println(e.getMessage());
      return "";
    }
    return resp.getId();
  }

}

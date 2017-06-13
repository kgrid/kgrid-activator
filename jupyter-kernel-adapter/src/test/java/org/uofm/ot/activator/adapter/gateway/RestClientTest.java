package org.uofm.ot.activator.adapter.gateway;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

/**
 * Created by grosscol on 2017-06-13.
 */
@RunWith(HierarchicalContextRunner.class)
public class RestClientTest {

  private RestClient client;
  private MockRestServiceServer mockServer;
  private RestTemplate restTemplate;

  public String jsonFixture(String fixtureName) throws IOException {
    String json = new Scanner(
        RestClientTest.class.getResourceAsStream("/fixtures/" + fixtureName + ".json"), "UTF-8")
        .useDelimiter("\\A").next();
    return json;
  }

  @Before
  public void setup() {
    try {
      client = new RestClient();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    restTemplate = client.restTemplate;
    mockServer = MockRestServiceServer.bindTo(restTemplate).build();
  }

  @Test
  public void getVersion() throws Exception {
    String response = jsonFixture("get-api");
    mockServer.expect(requestTo("http://localhost:8888/api"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));
    String version = client.gatewayVersion();

    mockServer.verify();
    assertThat(version, equalTo("5.0.0"));
  }

  // Main Context is that Jupytr Gateway is accessible and client is authorized.
  @Test
  public void startTest() throws Exception {
    String response = jsonFixture("post-kernels");
    mockServer.expect(requestTo("http://localhost:8888/api/kernels"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));
    String kernel_id = client.startKernel();

    mockServer.verify();
    assertThat(kernel_id, equalTo("deadbeef-1234-5678-90ab-beefbeefbeef"));
  }

  @Test
  public void listKernels() throws Exception {
    String response = jsonFixture("get-kernels");
    mockServer.expect(requestTo("http://localhost:8888/api/kernels"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));
    List<RestResponse> kernels = client.getKernels();

    assertThat(kernels, hasSize(2));
  }

  // When Jupytr Gateway accessible and client is NOT authorized
  public class ClientNotAuthorized {

    RestClient client;
    MockRestServiceServer mockServer;
    RestTemplate restTemplate;

    @Before
    public void setupUnauthorized() {
      try {
        client = new RestClient();
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
      restTemplate = client.restTemplate;
      mockServer = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @Test
    public void kernelsNotListed() throws Exception {
      String response = jsonFixture("get-kernels-forbidden");
      mockServer.expect(requestTo("http://localhost:8888/api/kernels"))
          .andExpect(method(HttpMethod.GET))
          .andRespond(withStatus(HttpStatus.FORBIDDEN).body(response));
      List<RestResponse> kernels = client.getKernels();

      assertThat(kernels, hasSize(0));
    }
  }

  // When Jupytr Gateway NOT accessible.
  public class GatewayNotAccessible {

    RestClient client;

    @Before
    public void setupInaccessible() {
      try {
        URI nonExistantHost = new URI("not-extant.local:8888");
        client = new RestClient(nonExistantHost);
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
    }

    @Test
    public void kernelsInaccessible() throws Exception {
      List<RestResponse> kernels = client.getKernels();
      assertThat(kernels, hasSize(0));
    }
  }
}

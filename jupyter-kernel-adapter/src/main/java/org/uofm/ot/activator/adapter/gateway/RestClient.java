package org.uofm.ot.activator.adapter.gateway;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Created by grosscol on 2017-06-13.
 */
public class RestClient {
  private URI restURI;
  public RestTemplate restTemplate;

  public RestClient() {
    this(URI.create("http://localhost:8888"));
  }

  public RestClient(final URI restURI) {
    this.restTemplate = new RestTemplate();
    this.restURI = restURI;
  }

  public String gatewayVersion() {
    HashMap<String, Object> jb = new HashMap<>();
    URI targetURI = restURI.resolve("/api");
    try {
      jb = restTemplate.getForObject(targetURI, HashMap.class);
    } catch (ResourceAccessException e){
      System.out.println(e.getMessage());
    }
    return (String) jb.get("version");
  }

  public List<KernelMetadata> getKernels() {
    List<KernelMetadata> list = new ArrayList<>();
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
    KernelMetadata resp;
    try {
      URI targetUri = new URI("http://localhost:8888/api/kernels");
      String request = "{\"name\": \"python\"}";
      resp = restTemplate.postForObject(targetUri, request, KernelMetadata.class);
    } catch (URISyntaxException | HttpClientErrorException | ResourceAccessException e) {
      System.out.println(e.getMessage());
      return "";
    }
    return resp.getId();
  }
}

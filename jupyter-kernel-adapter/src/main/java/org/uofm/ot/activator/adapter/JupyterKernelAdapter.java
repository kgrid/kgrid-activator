package org.uofm.ot.activator.adapter;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.uofm.ot.activator.adapter.gateway.KernelMetadata;
import org.uofm.ot.activator.adapter.gateway.RestClient;
import org.uofm.ot.activator.adapter.gateway.SockPuppet;
import org.uofm.ot.activator.adapter.gateway.WebSockHeader;
import org.uofm.ot.activator.adapter.gateway.WebSockMessage;
import org.uofm.ot.activator.exception.OTExecutionStackException;

public class JupyterKernelAdapter implements ServiceAdapter {

  public RestClient restClient;
  public SockPuppet sockClient;
  String host;
  String port;
  long maxDuration;
  long pollDuration;

  public JupyterKernelAdapter() {
    host = "localhost";
    port = "8888";
    URI restUri = URI.create("http://" + host + ":" + port);
    restClient = new RestClient(restUri);
    sockClient = new SockPuppet();
    maxDuration = 10_000_000_000L;
    pollDuration = 500_000_000L;
  }

  public Object execute(Map<String, Object> args, String code, String functionName,
      Class returnType)
      throws OTExecutionStackException {
    if (code == "") {
      throw new OTExecutionStackException(functionName + " No code to execute ");
    }

    // Obtain kernel suitable for running python code
    KernelMetadata selectedKernel = selectKernel();
    if (selectedKernel == null) {
      throw new OTExecutionStackException(" no available Jupyter Kernel for payload ");
    }

    // Connect to WebSocket
    URI sockUri = URI.create(
        String.format("ws://%s:%s/api/kernels/%s/channels", host, port, selectedKernel.getId()));
    sockClient.connectToServer(sockUri);

    // Send Payload
    WebSockHeader reqHeader = sockClient.sendPayload(code);

    // Send call to execute function in payload
    WebSockHeader execHeader = sockClient.sendPayload("print( "+functionName+"())");

    // Poll for responses
    Object result;
    Optional<WebSockMessage> response = pollForResultMessage(execHeader);
    if (response.isPresent()) {
      result = responseToResult(response.get());
    } else {
      throw new OTExecutionStackException("No result returned in time");
    }

    return returnType.cast(result);
  }

  // To which kernel should the code be sent?
  public KernelMetadata selectKernel() {
    KernelMetadata kernel;

    //Get list of kernels that match criteria
    List<KernelMetadata> kernels = restClient.getKernels();
    Optional<KernelMetadata> selectedKernel = kernels.stream()
        .filter(metadata -> metadata.getName().contains("python"))
        .findFirst();

    //Ask to create a new kernel if one is not available.
    if (selectedKernel.isPresent()) {
      kernel = selectedKernel.get();
    } else {
      kernel = restClient.startKernel();
    }

    return kernel;
  }

  public Optional<WebSockMessage> pollForResultMessage(WebSockHeader reqHeader) {
    WebSockMessage result = null;
    WebSockMessage msg = null;
    long duration = maxDuration - pollDuration;
    long elapsed = 0L;
    long start = System.nanoTime();

    // timeElapsed < duration
    while (elapsed < duration && result == null) {
      try {
        msg = sockClient.getMessageQ().poll(pollDuration, TimeUnit.NANOSECONDS);
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      }
      if (msg != null && (msg.isError() || msg.isResult()) ) {
        result = msg;
      }
      elapsed = System.nanoTime() - start;
    }
    return Optional.ofNullable(result);
  }

  public Object responseToResult(WebSockMessage msg) {
    return msg.content.get("text");
  }


  public List<String> supports() {
    List<String> languages = new ArrayList<>();
    languages.add("Python");
    return languages;
  }
}

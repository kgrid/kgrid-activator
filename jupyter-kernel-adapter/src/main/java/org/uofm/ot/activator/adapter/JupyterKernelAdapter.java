package org.uofm.ot.activator.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.uofm.ot.activator.adapter.gateway.KernelMetadata;
import org.uofm.ot.activator.adapter.gateway.RestClient;
import org.uofm.ot.activator.adapter.gateway.SockPuppet;
import org.uofm.ot.activator.adapter.gateway.SockResponseProcessor;
import org.uofm.ot.activator.adapter.gateway.WebSockHeader;
import org.uofm.ot.activator.exception.OTExecutionStackException;

@Component
public class JupyterKernelAdapter implements ServiceAdapter {

  public RestClient restClient;
  public SockPuppet sockClient;
  public SockResponseProcessor msgProcessor;

  @Value("${ipython.kernelgateway.host}")
  public String host;
  @Value("${ipython.kernelgateway.port}")
  public String port;
  @Value("${ipython.kernelgateway.maxDuration}")
  long maxDuration;
  @Value("${ipython.kernelgateway.pollInterval}")
  long pollInterval;

  public JupyterKernelAdapter() {
    URI restUri = URI.create("http://" + host + ":" + port);
    restClient = new RestClient(restUri);
    sockClient = new SockPuppet();
    msgProcessor = new SockResponseProcessor(sockClient.getMessageQ());
  }

  public Object execute(Map<String, Object> args, String code, String functionName,
      Class returnType)
      throws OTExecutionStackException {
    if (code == null || code.isEmpty()) {
      throw new OTExecutionStackException(" No code to execute ");
    }
    if (functionName == null || functionName.isEmpty()) {
      throw new OTExecutionStackException(" No function name to execute ");
    }

    // Obtain kernel suitable for running python code
    KernelMetadata selectedKernel = selectKernel();
    if (selectedKernel == null) {
      throw new OTExecutionStackException(" No available Jupyter Kernel for payload ");
    }

    // Connect to WebSocket
    URI sockUri = URI.create(
        String.format("ws://%s:%s/api/kernels/%s/channels", host, port, selectedKernel.getId()));
    sockClient.connectToServer(sockUri);

    // Send code to allow JSON output from IPython kernel:
    sockClient.sendPayload("from IPython.display import JSON");

    // Send knowledge object function code to the kernel
    WebSockHeader reqHeader = sockClient.sendPayload(code);

    // Send payload to call the kobject function
    sockClient.sendPayload(buildCallingPayload(args, functionName));

    // Instruct IPython to serialize the result as json
    sockClient.sendPayload("JSON(result)");

    // Poll for responses
    msgProcessor.beginProcessing(maxDuration, pollInterval);

    if (msgProcessor.encounteredError()) {
      throw new OTExecutionStackException(
          "Error in exec environment: " + msgProcessor.getErrorMsg());
    } else if (msgProcessor.encounteredTimeout()) {
      throw new OTExecutionStackException(
          "Timeout occurred. Max duration reached before result returned.");
    }

    return msgProcessor.getResult();
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

  // Generate payload for calling kobject function.
  public String buildCallingPayload(Map args, String functionName) {
    String payload;

    if (args.isEmpty()) {
      payload = "result = " + functionName + "()";
    } else {
      try {
        StringBuilder sb = new StringBuilder();
        sb.append("import json\n")
            .append(String
                .format("args = json.loads('%s')", new ObjectMapper().writeValueAsString(args)))
            .append("\n")
            .append(String.format("result = %s(args)", functionName));
        payload = sb.toString();
      } catch (JsonProcessingException e) {
        throw new OTExecutionStackException("Error serializing args.", e);
      }
    }

    return payload;
  }

  public List<String> supports() {
    List<String> languages = new ArrayList<>();
    languages.add("Python");
    return languages;
  }
}

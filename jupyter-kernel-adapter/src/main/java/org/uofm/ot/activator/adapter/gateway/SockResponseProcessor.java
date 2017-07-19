package org.uofm.ot.activator.adapter.gateway;

import com.sun.istack.internal.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Watches a synchronized message Q for specified duration
 * until an answer or an error is encountered.
 *
 * Created by grosscol on 2017-07-17.
 */
public class SockResponseProcessor {
  private ArrayBlockingQueue<WebSockMessage> externalQ;
  private WebSockMessage resultMessage = null;
  private boolean finishedProcessing = false;
  private String errorMsg = null;

  public SockResponseProcessor(ArrayBlockingQueue<WebSockMessage> queue){
    externalQ = queue;
  }

  public void beginProcessing(long maxDuration, long pollInterval){
    reset();

    WebSockMessage msg = null;
    long duration = maxDuration - pollInterval;

    // Ignore negative durations
    if(duration < 1){ duration = 1; }

    long elapsed = 0L;
    long start = System.nanoTime();

    while (elapsed < duration && resultMessage == null) {
      try {
        msg = externalQ.poll(pollInterval, TimeUnit.NANOSECONDS);
      } catch (InterruptedException ex) {
        ex.printStackTrace();
        errorMsg = "Processing websocket messages interrupted.";
        break;
      }
      if (msg != null && (msg.isError() || msg.isResult()) ) {
        resultMessage = msg;
      }
      elapsed = System.nanoTime() - start;
    }
    finishedProcessing = true;
  }

  public void reset(){
    resultMessage = null;
    finishedProcessing = false;
  }

  public boolean encounteredError(){
    return errorMsg != null || (resultMessage != null && resultMessage.isError());
  }

  public boolean encounteredResult(){
    return resultMessage != null && resultMessage.isResult();
  }

  public boolean encounteredTimeout(){
    return resultMessage == null && finishedProcessing == true;
  }

  @Nullable
  public Map getResult(){
    if(!encounteredResult()){ return null; }
    return (Map) resultMessage.content.data.get("application/json");
  }

  @Nullable
  public String getErrorMsg(){
    if(!encounteredError()){ return null; }
    return resultMessage.content.ename + " : " + resultMessage.content.evalue;
  }

}

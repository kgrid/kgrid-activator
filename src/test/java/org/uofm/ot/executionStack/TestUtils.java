package org.uofm.ot.executionStack;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.Charset;
import org.springframework.http.MediaType;

/**
 * Created by nggittle on 3/28/17.
 */
public class TestUtils {

  public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset
      .forName("utf8"));

  public static final MediaType APPLICATION_TEXT_UTF8 = new MediaType(MediaType.TEXT_PLAIN.getType(), MediaType.TEXT_PLAIN.getSubtype(), Charset
      .forName("utf8"));

  public static byte[] convertObjectToJsonBytes(Object object) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    return mapper.writeValueAsBytes(object);
  }

}

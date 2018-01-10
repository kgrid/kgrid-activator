package edu.umich.lhs.activator.services;

import edu.umich.lhs.activator.domain.Kobject;
import java.io.IOException;
import java.io.OutputStream;
import org.springframework.http.MediaType;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

/**
 * Created by grosscol on 2018-01-09.
 */
@Component
public class KobjectConverter
    extends AbstractHttpMessageConverter<Kobject> {

  public KobjectConverter() {
    super(MediaType.APPLICATION_JSON_UTF8,
        MediaType.APPLICATION_JSON,
        new MediaType("applicaion","ld+json")
        );
  }

  @Override
  public boolean canWrite(Class<?> clazz, org.springframework.http.MediaType mediaType) {
    return false;
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return Kobject.class.isAssignableFrom(clazz);
  }

  @Override
  protected Kobject readInternal(Class<? extends Kobject> clazz, HttpInputMessage inputMessage)
      throws IOException, HttpMessageNotReadableException {
    Kobject kob = KobjectImporter.jsonToKobject(inputMessage.getBody());
    return kob;
  }

  //Do more than a dummy implementation in order to write Kobjects to ouput stream.
  @Override
  protected void writeInternal(Kobject kob, HttpOutputMessage outputMessage)
      throws IOException, HttpMessageNotWritableException {
    try {
      OutputStream outputStream = outputMessage.getBody();
      String body = kob.toString();
      outputStream.write(body.getBytes());
      outputStream.close();
    } catch (Exception e) {
    }
  }
}


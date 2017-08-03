package edu.umich.lhs.activator;

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
  public static final String INPUT_SPEC_ONE_INPUT
      = "<rdf:RDF xmlns:ot='http://uofm.org/objectteller/' xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'>"
      + "<rdf:Description rdf:about='http://uofm.org/objectteller/inputMessage'>"
      + "<ot:noofparams>1</ot:noofparams>"
      + "<ot:params>"
      + "<rdf:Seq>"
      + "<rdf:li>rxcui</rdf:li>"
      + "</rdf:Seq>"
      + "</ot:params>"
      + "</rdf:Description>"
      + "<rdf:Description rdf:about='http://uofm.org/objectteller/rxcui/'>"
      + "<ot:datatype>MAP</ot:datatype>"
      + "</rdf:Description>"
      + "</rdf:RDF>";
  public static final String INPUT_SPEC_TWO_INPUTS
      = "<rdf:RDF xmlns:ot='http://uofm.org/objectteller/' xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'>"
      + "<rdf:Description rdf:about='http://uofm.org/objectteller/inputMessage'>"
      + "<ot:noofparams>2</ot:noofparams>"
      + "<ot:params>"
      + "<rdf:Seq>"
      + "<rdf:li>rxcui</rdf:li>"
      + "<rdf:li>rxcui2</rdf:li>"
      + "</rdf:Seq>"
      + "</ot:params>"
      + "</rdf:Description>"
      + "<rdf:Description rdf:about='http://uofm.org/objectteller/rxcui/'>"
      + "<ot:datatype>MAP</ot:datatype>"
      + "</rdf:Description>"
      + "</rdf:RDF>";
  public static final String OUTPUT_SPEC_RET_STR
          = "<rdf:RDF xmlns:ot='http://uofm.org/objectteller/' xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'>"
          + "<rdf:Description rdf:about='http://uofm.org/objectteller/outputMessage'>"
          + "<ot:returntype>STRING</ot:returntype> </rdf:Description> </rdf:RDF>";
  public static final String OUTPUT_SPEC_RET_INT
              = "<rdf:RDF xmlns:ot='http://uofm.org/objectteller/' xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'>"
              + "<rdf:Description rdf:about='http://uofm.org/objectteller/outputMessage'>"
              + "<ot:returntype>INT</ot:returntype> </rdf:Description> </rdf:RDF>";

  public static final String CODE = "function execute(a){ return a.toString()}";

  public static byte[] convertObjectToJsonBytes(Object object) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    return mapper.writeValueAsBytes(object);
  }

}

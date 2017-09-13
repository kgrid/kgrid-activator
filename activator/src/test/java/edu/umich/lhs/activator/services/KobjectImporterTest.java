package edu.umich.lhs.activator.services;

import edu.umich.lhs.activator.domain.ArkId;
import edu.umich.lhs.activator.domain.DataType;
import edu.umich.lhs.activator.domain.ParamDescription;
import edu.umich.lhs.activator.domain.Payload;
import java.util.ArrayList;
import org.apache.jena.graph.Graph;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Model;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isOneOf;
import static org.junit.Assert.*;

import edu.umich.lhs.activator.TestUtils;
import edu.umich.lhs.activator.domain.Kobject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Seq;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.util.MonitorModel;
import org.apache.jena.vocabulary.RDF;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


/**
 * Created by grosscol on 2017-09-12.
 */
public class KobjectImporterTest {

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  private final String SAMPLE_JSON_KOBJECT = TestUtils.safeLoadFixture("kobject-sample.json");

  private InputStream inStream;
  private Model model;
  private Resource kobjectRdf;

  @Before
  public void setUp() throws Exception {
    inStream = new ByteArrayInputStream(SAMPLE_JSON_KOBJECT.getBytes());
    model = ModelFactory.createDefaultModel();
    kobjectRdf = model.createResource("http://example.com/001", KobjectImporter.kobjectRDFClass);
  }

  // Behavior tests
  @Test
  public void smokeTest() throws Exception {
    KobjectImporter.jsonToKobject(inStream);
  }

  @Test
  public void makesOneOfManyKobjects() throws Exception {
    InputStream ins = TestUtils.streamFixture("kobject-multiple.json");
    Kobject kob = KobjectImporter.jsonToKobject(ins);
    assertThat(kob.getIdentifier().getArkId(), equalTo("ark:/1234/56second"));
  }


  // Unit tests
  @Test
  public void getSingleKobject() throws Exception {
    Resource kobjectOne = model.createResource("http://example.com/001", KobjectImporter.kobjectRDFClass);
    Resource kobjectTwo = model.createResource("http://example.com/002", KobjectImporter.kobjectRDFClass);

    Resource result = KobjectImporter.getSingleKobjectResource(model);
    assertThat(result, isOneOf(kobjectOne, kobjectTwo));
  }

  @Test
  public void deserializeIdentifier() throws Exception {
    String ark = "ark:/1234/foo";
    kobjectRdf.addProperty(KobjectImporter.identifierProp, ark);

    ArkId deserializedArk = KobjectImporter.deserializeIdentifier(kobjectRdf);

    ArkId checkArk = new ArkId(ark);
    assertThat(deserializedArk, equalTo(checkArk));
  }

  @Test
  public void deserializePayload() throws Exception {
    String content = "foo(){ print('Hello') }";
    String engineType = "SmallCapable";
    String funcName = "foo";

    Resource payloadRDF = model.createResource(new AnonId("1"));

    kobjectRdf.addProperty(KobjectImporter.payloadProp, payloadRDF);
    payloadRDF.addProperty(KobjectImporter.contentProp, content);
    payloadRDF.addProperty(KobjectImporter.engineTypeProp, engineType);
    payloadRDF.addProperty(KobjectImporter.funcNameProp, funcName);

    Payload p = KobjectImporter.deserializePayload(kobjectRdf);

    Payload check = new Payload();
    check.setContent(content);
    check.setEngineType(engineType);
    check.setFunctionName(funcName);

    assertThat(p.getContent(), equalTo(check.getContent()));
    assertThat(p.getEngineType(), equalTo(check.getEngineType()));
    assertThat(p.getFunctionName(), equalTo(check.getFunctionName()));
  }

  @Test
  public void deserializeNoofParams() throws Exception {
    Integer numParams = 2;
    Resource inputMessage = model.createResource(AnonId.create());

    kobjectRdf.addProperty(KobjectImporter.inpMsgProp, inputMessage);
    inputMessage.addLiteral(KobjectImporter.noOfParamsProp, numParams);

    Integer result = KobjectImporter.deserializeNoofParams(kobjectRdf);

    assertThat(result, equalTo(numParams));
  }

  @Test
  public void deserializeParameters() throws Exception {
    Resource inputMessage = model.createResource(AnonId.create());
    Seq paramSeq = model.createSeq();
    Resource param1 = model.createResource(AnonId.create());
    Resource param2 = model.createResource(AnonId.create());

    kobjectRdf.addProperty(KobjectImporter.inpMsgProp, inputMessage);
    inputMessage.addProperty(KobjectImporter.hasParams, paramSeq);

    param1.addLiteral(KobjectImporter.paramNameProp, "foo")
        .addLiteral(KobjectImporter.dataTypeProp, "STRING");
    param2.addLiteral(KobjectImporter.paramNameProp, "bar")
        .addLiteral(KobjectImporter.dataTypeProp, "INT");
    paramSeq.add(param1)
        .add(param2);

    ParamDescription p1 = new ParamDescription("foo", DataType.STRING , null, null);
    ParamDescription p2 = new ParamDescription("bar", DataType.INT, null, null);

    ArrayList<ParamDescription> pList = KobjectImporter.deserializeParameters(kobjectRdf);

    assertThat(pList.size(), equalTo(2));
  }

  @Test
  public void deserializeParamDescription() throws Exception {
    //TODO
  }

  @Test
  public void deserializeReturnType() throws Exception {
    //TODO
  }

}
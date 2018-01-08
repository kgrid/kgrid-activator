package edu.umich.lhs.activator.services;


import edu.umich.lhs.activator.domain.ArkId;
import edu.umich.lhs.activator.domain.DataType;
import edu.umich.lhs.activator.domain.Kobject;
import edu.umich.lhs.activator.domain.ParamDescription;
import edu.umich.lhs.activator.domain.Payload;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.EnumUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;

/**
 * Created by grosscol on 2017-09-11.
 *
 * Conversion from JSON-LD to Kobject
 * Accomplished by creating an RDF model from a JSON-LD input and then extracting relevant fields.
 */
public class KobjectImporter {

  // Knoweldge Grid namespace
  private static final String KGRID_NS = "http://lhs.umich.edu/kgrid#";
  // Predicates
  public static final String KNOWLEDGE_OBJECT_URI = KGRID_NS + "knowledgeObject";
  public static final String HAS_INPUT_MESSAGE_URI = KGRID_NS + "hasInputMessage";
  public static final String HAS_OUTPUT_MESSAGE_URI = KGRID_NS + "hasOutputMessage";
  public static final String HAS_PAYLOAD_URI = KGRID_NS + "hasPayload";
  public static final String NUM_OF_PARAMS_URI = KGRID_NS + "noofparams";
  public static final String HAS_PARAMS_URI = KGRID_NS + "hasParams";
  public static final String PARAM_NAME_URI = KGRID_NS + "paramname";
  public static final String DATA_TYPE_URI = KGRID_NS + "datatype";
  public static final String RETURN_TYPE_URI = KGRID_NS + "returnType";
  public static final String FUNCTION_NAME_URI = KGRID_NS + "functionName";
  public static final String CONTENT_URI = KGRID_NS + "content";
  public static final String ENGINE_TYPE_URI = KGRID_NS + "engineType";
  public static final String IDENTIFIER_URI = "http://schema.org/identifier";

  // Reference Resource
  public static final Resource kobjectRDFClass = ResourceFactory
      .createResource(KNOWLEDGE_OBJECT_URI);

  // Reference Properties
  public static final Property payloadProp = ResourceFactory.createProperty(HAS_PAYLOAD_URI);
  public static final Property funcNameProp = ResourceFactory.createProperty(FUNCTION_NAME_URI);
  public static final Property engineTypeProp = ResourceFactory.createProperty(ENGINE_TYPE_URI);
  public static final Property contentProp = ResourceFactory.createProperty(CONTENT_URI);
  public static final Property inpMsgProp = ResourceFactory.createProperty(HAS_INPUT_MESSAGE_URI);
  public static final Property outMsgProp = ResourceFactory.createProperty(HAS_OUTPUT_MESSAGE_URI);
  public static final Property identifierProp = ResourceFactory.createProperty(IDENTIFIER_URI);
  public static final Property noOfParamsProp = ResourceFactory.createProperty(NUM_OF_PARAMS_URI);
  public static final Property hasParams = ResourceFactory.createProperty(HAS_PARAMS_URI);
  public static final Property paramNameProp = ResourceFactory.createProperty(PARAM_NAME_URI);
  public static final Property dataTypeProp = ResourceFactory.createProperty(DATA_TYPE_URI);
  public static final Property retTypeProp = ResourceFactory.createProperty(RETURN_TYPE_URI);

  // Mapping DataType to Class
  static final Map<DataType, Class> dataClasses = mapClasses();

  static Map<DataType, Class> mapClasses() {
    HashMap<DataType, Class> m = new HashMap<>();
    m.put(DataType.INT, Integer.class);
    m.put(DataType.LONG, Long.class);
    m.put(DataType.FLOAT, float.class);
    m.put(DataType.STRING, String.class);
    m.put(DataType.MAP, Map.class);
    return m;
  }

  public static Kobject jsonToKobject(String ins) {
    ByteArrayInputStream stream = new ByteArrayInputStream(ins.getBytes());
    return jsonToKobject(stream);
  }

  public static Kobject jsonToKobject(InputStream ins) {
    Model model = jsonToModel(ins);
    return extractKobject(model);
  }

  static Kobject extractKobject(Model model) {
    Kobject kob = new Kobject();
    Resource kobjectRdf = getSingleKobjectResource(model);

    Payload payload = deserializePayload(kobjectRdf);
    ArkId identifier = deserializeIdentifier(kobjectRdf);
    Integer numParams = deserializeNoofParams(kobjectRdf);
    ArrayList<ParamDescription> paramDescriptions = deserializeParameters(kobjectRdf);
    Class returnClass = deserializeReturnType(kobjectRdf);

    kob.setNoofParams(numParams);
    kob.setParamDescriptions(paramDescriptions);
    kob.setReturnType(returnClass);
    kob.setIdentifier(identifier);
    kob.setPayload(payload);
    kob.setRdfModel(model);
    return kob;

  }

  /**
   * Take json-ld serialization of Knowledge Object(s) and deserialize to RDF model
   *
   * @param ins json-ld input stream
   * @return RDFModel
   */
  static Model jsonToModel(InputStream ins) {
    Model model = ModelFactory.createDefaultModel();
    // TODO Auto-generated constructor stub
    model.read(ins, "http://example.com", "JSON-LD");

    return model;
  }

  static Resource getSingleKobjectResource(Model model) {
    ResIterator itt = model.listResourcesWithProperty(RDF.type, kobjectRDFClass);
    if (itt.hasNext()) {
      return itt.nextResource();
    } else {
      // Throw exception or return null?
      // TODO: Return exception
      return null;
    }
  }

  /**
   * Deserialize the identifier (arkida) out of a rdf knowledge object resource
   */
  static ArkId deserializeIdentifier(Resource rdfKobject) {
    return (new ArkId(rdfKobject.getProperty(identifierProp).getString()));
  }

  /**
   * Deserialize payload out of a rdf knowledge object resource
   */
  static Payload deserializePayload(Resource rdfKobject) {
    Resource rdfPayload = rdfKobject.getPropertyResourceValue(payloadProp);

    Payload payload = new Payload();
    payload.setFunctionName(rdfPayload.getProperty(funcNameProp).getString());
    payload.setEngineType(rdfPayload.getProperty(engineTypeProp).getString());
    payload.setContent(rdfPayload.getProperty(contentProp).getString());

    return payload;
  }

  /**
   * Deserialize parameter descriptions out of a rdf knowledge object resource
   */
  static Integer deserializeNoofParams(Resource rdfKobject) {
    Resource inpMsg = rdfKobject.getPropertyResourceValue(inpMsgProp);
    return inpMsg.getProperty(noOfParamsProp).getInt();
  }

  /**
   * Deserialize parameter descriptions out of a rdf knowledge object resource
   */
  static ArrayList<ParamDescription> deserializeParameters(Resource rdfKobject) {
    ArrayList<ParamDescription> params = new ArrayList<>();

    // Get input message as intermediate step
    Resource inpMsg = rdfKobject.getPropertyResourceValue(inpMsgProp);

    NodeIterator itt = inpMsg.getProperty(hasParams).getSeq().iterator();
    while (itt.hasNext()) {
      RDFNode node = itt.next();
      if (node.isResource()) {
        params.add(deserializeParamDescription(node.asResource()));
      }
    }
    return params;
  }

  /**
   * Deserialize single parameter descriptions out of a rdf parameter resource
   */
  static ParamDescription deserializeParamDescription(Resource rdfParam) {
    return (new ParamDescription(
        rdfParam.getProperty(paramNameProp).getString(),
        DataType.valueOf(rdfParam.getProperty(dataTypeProp).getString()),
        null, null)
    );
  }

  /**
   * Deserialize return class from rdf knowledge object resource
   */
  static Class deserializeReturnType(Resource rdfKobject) {
    Resource outMsg = rdfKobject.getPropertyResourceValue(outMsgProp);

    String retTypeString = outMsg.getProperty(retTypeProp).getString();
    DataType retType = EnumUtils.getEnum(DataType.class, retTypeString);

    return (dataClasses.getOrDefault(retType, Object.class));
  }

}

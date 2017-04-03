package org.uofm.ot.activator.services;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.springframework.stereotype.Service;
import org.uofm.ot.activator.domain.DataType;
import org.uofm.ot.activator.domain.KnowledgeObject;
import org.uofm.ot.activator.domain.ParamDescription;

@Service
public class IoSpecGenerator {
	
	public static final String OT_NAMESPACE =  "http://uofm.org/objectteller/";

	public ioSpec covertInputOutputMessageToCodeMetadata(KnowledgeObject ko) {

		if(ko == null || ko.inputMessage == null || ko.outputMessage == null) {
			return null;
		}

		ioSpec codeMetadata = new ioSpec();

		Model modelInput = ModelFactory.createDefaultModel();

		InputStream streamInput = new ByteArrayInputStream(ko.inputMessage.getBytes(StandardCharsets.UTF_8));

		modelInput.read(streamInput,null);

		StmtIterator iterInput = modelInput.getResource(OT_NAMESPACE + "inputMessage").listProperties();
		ArrayList<String> params = new ArrayList<String>();

		while (iterInput.hasNext()) {

			Statement stmt = iterInput.nextStatement();

			if("noofparams".equals(stmt.getPredicate().getLocalName()))
				codeMetadata.setNoOfParams(Integer.parseInt(stmt.getObject().toString()));

			if("params".equals(stmt.getPredicate().getLocalName())){
				NodeIterator props = modelInput.getSeq(stmt.getResource()).iterator();
				while(props.hasNext()){
					RDFNode st = props.nextNode();
					if(st.isLiteral())
						params.add(st.asLiteral().getString());
				}
			}
		}

		ArrayList<ParamDescription> paramList = new ArrayList<ParamDescription>();
		for (String param : params) {
			StmtIterator paramDesc = modelInput.getResource(OT_NAMESPACE+param+"/").listProperties();
			ParamDescription description = new ParamDescription();
			description.setName(param);
			while(paramDesc.hasNext()){
				Statement stmt = paramDesc.nextStatement();
				if("datatype".equals(stmt.getPredicate().getLocalName()))
					description.setDatatype(DataType.valueOf(stmt.getObject().toString()));
				if("min".equals(stmt.getPredicate().getLocalName()))
					description.setMin(Integer.parseInt(stmt.getObject().toString()));
				if("max".equals(stmt.getPredicate().getLocalName()))
					description.setMax(Integer.parseInt(stmt.getObject().toString()));
			}
			paramList.add(description);
		}

		codeMetadata.setParams(paramList);

		Model modelOutput = ModelFactory.createDefaultModel();

		InputStream streamOutput = new ByteArrayInputStream(ko.outputMessage.getBytes(StandardCharsets.UTF_8));

		modelOutput.read(streamOutput,null);

		StmtIterator iterOutput = modelOutput.getResource(OT_NAMESPACE + "outputMessage").listProperties();

		while (iterOutput.hasNext()) {

			Statement stmt = iterOutput.nextStatement();

			if("returntype".equals(stmt.getPredicate().getLocalName()))
				codeMetadata.setReturntype(DataType.valueOf(stmt.getObject().toString()));

		}

		return codeMetadata;

	}
}

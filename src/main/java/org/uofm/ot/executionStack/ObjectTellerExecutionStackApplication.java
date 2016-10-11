package org.uofm.ot.executionStack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.uofm.ot.executionStack.exception.OTExecutionStackException;
import org.uofm.ot.executionStack.objectTellerLayer.ObjectTellerInterface;
import org.uofm.ot.executionStack.transferObjects.KnowledgeObjectDTO;

@SpringBootApplication
public class ObjectTellerExecutionStackApplication   {

	public static void main(String[] args) {
		SpringApplication.run(ObjectTellerExecutionStackApplication.class, args);
	}

}

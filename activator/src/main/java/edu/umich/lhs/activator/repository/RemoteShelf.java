package edu.umich.lhs.activator.repository;


import edu.umich.lhs.activator.exception.ActivatorException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import edu.umich.lhs.activator.exception.BadGatewayException;
import edu.umich.lhs.activator.domain.ArkId;
import edu.umich.lhs.activator.domain.KnowledgeObject;


@Service
public class RemoteShelf {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public KnowledgeObject checkOutByArkId(ArkId arkId) throws ActivatorException {
		RestTemplate rt = new RestTemplate();

		KnowledgeObject object = null;
		
		try {

			// This creates a client that redirects on gets for HTTP 3xx redirect responses.
			HttpClient instance = HttpClientBuilder.create()
					.setRedirectStrategy(new DefaultRedirectStrategy()).build();

			RestTemplate rest = new RestTemplate(new HttpComponentsClientHttpRequestFactory(instance));

			ResponseEntity<KnowledgeObject> response = rest.getForEntity(getAbsoluteObjectUrl(arkId) + "/complete", KnowledgeObject.class);

			object = response.getBody() ; 

			object.url = getAbsoluteObjectUrl(arkId) ; 

			log.info("KnowledgeObject with Ark Id: "+ arkId + "is checked out from : "+ getAbsoluteObjectUrl(arkId) );
		} catch ( HttpClientErrorException e ) {
			if(e.getRawStatusCode() == HttpStatus.NOT_FOUND.value() ){
				throw new BadGatewayException("Object with Ark Id : "+arkId+" does not exist ");
			} else {
				throw new ActivatorException(e);
			}
		}
		
		
		return object;
	}


	@Value("${library.url:http://n2t.net/}")
	String libraryAbsolutePath;

	public String getLibraryPath() {

			return libraryAbsolutePath;
	}

	public String getRemoteObjectURL(ArkId arkId) {
		if(libraryAbsolutePath.endsWith("/")){
			return libraryAbsolutePath + arkId.getArkId();
		}
		return libraryAbsolutePath + "/" + arkId.getArkId();
	}

	public String getAbsoluteObjectUrl(ArkId arkId) {
		return getLibraryPath()+"/knowledgeObject/" +arkId.getArkId();
	}
}

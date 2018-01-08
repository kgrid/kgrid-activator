package edu.umich.lhs.activator.repository;


import edu.umich.lhs.activator.domain.ArkId;
import edu.umich.lhs.activator.domain.KnowledgeObject;
import edu.umich.lhs.activator.domain.Kobject;
import edu.umich.lhs.activator.exception.ActivatorException;
import edu.umich.lhs.activator.exception.BadGatewayException;
import edu.umich.lhs.activator.services.KobjectImporter;
import java.util.Collections;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


@Service
public class RemoteShelf {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public Kobject checkOutByArkId(ArkId arkId) throws ActivatorException {

		Kobject kob;
		String url = getAbsoluteObjectUrl(arkId) + "/complete";

		try {
			// This creates a client that redirects on gets for HTTP 3xx redirect responses.
			HttpClient instance = HttpClientBuilder.create()
					.setRedirectStrategy(new DefaultRedirectStrategy()).build();

			RestTemplate rest = new RestTemplate(new HttpComponentsClientHttpRequestFactory(instance));

			// Only accept JSON
			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

			// Expect back a string of json
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			ResponseEntity<String> response = rest.exchange(url, HttpMethod.GET, entity, String.class);

			kob = KobjectImporter.jsonToKobject(response.getBody());
			kob.setUrl(getAbsoluteObjectUrl(arkId));

			log.info("KnowledgeObject with Ark Id: "+ arkId + "is checked out from : "+ getAbsoluteObjectUrl(arkId) );
		} catch ( HttpClientErrorException e ) {
			if(e.getRawStatusCode() == HttpStatus.NOT_FOUND.value() ){
				throw new BadGatewayException("Object with Ark Id : "+arkId+" does not exist ");
			} else {
				throw new ActivatorException(e);
			}
		}
		
		return kob;
	}

	public boolean libraryObjectExists(ArkId arkId) {
		try {
			HttpClient instance = HttpClientBuilder.create()
					.setRedirectStrategy(new DefaultRedirectStrategy()).build();

			RestTemplate rest = new RestTemplate(new HttpComponentsClientHttpRequestFactory(instance));

			ResponseEntity<KnowledgeObject> response = rest.getForEntity(getAbsoluteObjectUrl(arkId) + "/complete", KnowledgeObject.class);

			return response.getStatusCode() == HttpStatus.OK;

		} catch (RestClientException e) {
			return false;
		}
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

	private String getAbsoluteObjectUrl(ArkId arkId) {
		return getLibraryPath()+"/knowledgeObject/" +arkId.getArkId();
	}
}

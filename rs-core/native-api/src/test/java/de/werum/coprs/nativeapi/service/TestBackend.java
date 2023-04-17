package de.werum.coprs.nativeapi.service;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import de.werum.coprs.nativeapi.config.NativeApiProperties;
import de.werum.coprs.nativeapi.rest.model.stac.StacItemCollection;

@ExtendWith(MockitoExtension.class)
public class TestBackend {
	@Mock
	private RestTemplate restTemplate;

	private NativeApiProperties properties = new NativeApiProperties();

	private ODataBackendServiceImpl backend;

	@BeforeEach
	public void init() {
		properties.setPripProtocol("http");
		properties.setPripHost("s1pro-prip-frontend-svc.processing.svc.cluster.local");
		properties.setPripPort(8080);
		properties.setExternalPripProtocol("http");
		properties.setExternalPripHost("coprs.werum.de/prip/odata/v1/");
		properties.setExternalPripPort(80);

		backend = new ODataBackendServiceImpl(properties, restTemplate);
	}

	@Test
	public void test() throws Exception {
		String body = Files.readString(Paths.get("src/test/resources/testdata.json"));
		ResponseEntity<String> entity = new ResponseEntity<String>(body, HttpStatus.OK);

		Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.any(), ArgumentMatchers.<Class<String>>any())).thenReturn(entity);

		StacItemCollection item = backend.queryOData("mockedanyways");
		System.out.println("stac:"+item);

	}
}

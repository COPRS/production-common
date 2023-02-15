package de.werum.coprs.nativeapi.service;

import org.junit.Assert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
 
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestODataUrlBuilder {
	@Autowired
	private ODataBackendServiceImpl backend;
	
	@Test
	public void testUrlBuilder() {

		String url1 = backend.buildPripQueryUrl(
				"ContentDate/Start gt 2010-10-18T14:33:00.000Z and ContentDate/End lt 2023-02-06T14:33:00.000 and contains(Name,'S3A_PRODUCT')",
				false);
		String url2 = backend.buildPripQueryUrl(
				"ContentDate/Start gt 2010-10-18T14:33:00.000Z and ContentDate/End lt 2023-02-06T14:33:00.000 and contains(Name,'S3A_PRODUCT')",
				true);
		
		System.out.println(url1);
		System.out.println(url2);

		Assert.assertEquals(
				"http://s1pro-prip-frontend-svc.processing.svc.cluster.local:8080/odata/v1/Products?$filter=ContentDate/Start gt 2010-10-18T14:33:00.000Z and ContentDate/End lt 2023-02-06T14:33:00.000 and contains(Name,'S3A_PRODUCT')&$top=100",
				url1);
		Assert.assertEquals(
				"http://s1pro-prip-frontend-svc.processing.svc.cluster.local:8080/odata/v1/Products?$filter=ContentDate/Start gt 2010-10-18T14:33:00.000Z and ContentDate/End lt 2023-02-06T14:33:00.000 and contains(Name,'S3A_PRODUCT')&$expand=Attributes,Quicklooks&$top=100",
				url2);
	}
}

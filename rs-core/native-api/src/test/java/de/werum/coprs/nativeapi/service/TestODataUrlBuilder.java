/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.werum.coprs.nativeapi.service;

import org.junit.Assert;
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
				false, 1, 100);
		String url2 = backend.buildPripQueryUrl(
				"ContentDate/Start gt 2010-10-18T14:33:00.000Z and ContentDate/End lt 2023-02-06T14:33:00.000 and contains(Name,'S3A_PRODUCT')",
				true, 1, 100);

		String url3 = backend.buildPripQueryUrl(
				"ContentDate/Start gt 2010-10-18T14:33:00.000Z and ContentDate/End lt 2023-02-06T14:33:00.000 and contains(Name,'S3A_PRODUCT')",
				true, 10, 100);

		Assert.assertEquals(
				"http://s1pro-prip-frontend-svc.processing.svc.cluster.local:8080/odata/v1/Products?$filter=ContentDate/Start gt 2010-10-18T14:33:00.000Z and ContentDate/End lt 2023-02-06T14:33:00.000 and contains(Name,'S3A_PRODUCT')&$top=100",
				url1);
		Assert.assertEquals(
				"http://s1pro-prip-frontend-svc.processing.svc.cluster.local:8080/odata/v1/Products?$filter=ContentDate/Start gt 2010-10-18T14:33:00.000Z and ContentDate/End lt 2023-02-06T14:33:00.000 and contains(Name,'S3A_PRODUCT')&$expand=Attributes,Quicklooks&$top=100",
				url2);

		Assert.assertEquals(
				"http://s1pro-prip-frontend-svc.processing.svc.cluster.local:8080/odata/v1/Products?$filter=ContentDate/Start gt 2010-10-18T14:33:00.000Z and ContentDate/End lt 2023-02-06T14:33:00.000 and contains(Name,'S3A_PRODUCT')&$expand=Attributes,Quicklooks&$top=100&$skip=900",
				url3);
	}
}

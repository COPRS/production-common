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

package esa.s1pdgs.cpoc.ingestion.worker.service;

import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.ingestion.worker.product.ProductService;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public final class TestIngestionWorkerService {

	Reporting reporting = ReportingUtils.newReportingBuilder(MissionId.S1).newReporting("Test");
	
	@Mock
	Logger logger;
	
	@Mock
	ProductService productService;
	
	@Before
	public final void setup() {
		MockitoAnnotations.initMocks(this);
	}

	/*
	@Test
	public final void testOnMessage() throws Exception {
		final IngestionJob ingestion = new IngestionJob();
		ingestion.setRelativePath("fooBar");
		ingestion.setPickupBaseURL("file:/tmp");
		ingestion.setProductName("fooBar");
		ingestion.setProductFamily(ProductFamily.AUXILIARY_FILE);
		
		final GenericMessageDto<IngestionJob> mess = new GenericMessageDto<IngestionJob>();
		mess.setId(123);
		mess.setInputKey("testKEy");
		mess.setBody(ingestion);
		
		final ProductService fakeProductService = new ProductService() {

			@Override
			public List<Product<IngestionEvent>> ingest(final ProductFamily family, final InboxAdapter inboxAdapter,
					final IngestionJob ingestion, final ReportingFactory reportingFactory) throws Exception {
				// TODO Auto-generated method stub
				return Collections.emptyList();
			}

			@Override
			public void markInvalid(final InboxAdapter inboxAdapter, final IngestionJob ingestion,
					final ReportingFactory reportingFactory) throws Exception {
				// TODO Auto-generated method stub
				
			}			

		};
		
		final IngestionWorkerServiceConfigurationProperties properties = new IngestionWorkerServiceConfigurationProperties();
		
		final IngestionWorkerService uut = new IngestionWorkerService(
				null, 
				ErrorRepoAppender.NULL, 
				properties, 
				fakeProductService,
				AppStatus.NULL
		);
		uut.onMessage(mess);
	}

	private final IngestionEvent newIngestionEvent(
			final String name,
			final String key,
			final ProductFamily family
	) {
		final IngestionEvent dto = new IngestionEvent();
		dto.setProductName(name);
		dto.setRelativePath(key);
		dto.setProductFamily(family);
		return dto;
	}
	
	@Test
	public final void testIdentifyAndUpload() throws Exception {
		final IngestionWorkerServiceConfigurationProperties properties = new IngestionWorkerServiceConfigurationProperties();
		final IngestionWorkerService uut = new IngestionWorkerService(
				mqiClient, 
				ErrorRepoAppender.NULL, 
				properties,
				productService,
				AppStatus.NULL
		);
		final GenericMessageDto<IngestionJob> message = new GenericMessageDto<>();
		message.setId(123L);
		message.setBody(null);
		final IngestionJob ingestionJob = new IngestionJob();
		ingestionJob.setProductFamily(ProductFamily.AUXILIARY_FILE);
		ingestionJob.setProductName("foo.bar");
		message.setBody(ingestionJob);
		
		final File file = new File("foo.bar");
		final Product<IngestionEvent> prod = new Product<>();
		prod.setFamily(ProductFamily.AUXILIARY_FILE);
		prod.setFile(file);	
		final IngestionEvent dto = newIngestionEvent(
				file.getName(), 
				"foo.bar", 
				ProductFamily.AUXILIARY_FILE
		);
		prod.setDto(dto);
		
		final IngestionResult expectedResult = new IngestionResult(Arrays.asList(prod), 0L);
		doReturn(expectedResult).when(productService).ingest(Mockito.eq(ProductFamily.AUXILIARY_FILE), Mockito.eq(ingestionJob), Mockito.any());
		
		final IngestionResult result = uut.identifyAndUpload(message, ingestionJob, ReportingFactory.NULL);
		assertEquals(expectedResult, result);
		verify(productService, times(1)).ingest(Mockito.eq(ProductFamily.AUXILIARY_FILE), Mockito.eq(ingestionJob), Mockito.any());
		verify(productService, never()).markInvalid(Mockito.any(), Mockito.any());
	}

	@Test
	public final void testPublish() throws AbstractCodedException {
		final IngestionWorkerService uut = new IngestionWorkerService(
				mqiClient, 
				ErrorRepoAppender.NULL, 
				new IngestionWorkerServiceConfigurationProperties(),
				productService,
				AppStatus.NULL
		);

		final GenericMessageDto<IngestionJob> message = new GenericMessageDto<>();
		message.setId(123L);
		message.setInputKey("inputKey");
		message.setBody(new IngestionJob());
		
		final IngestionEvent dto = new IngestionEvent();
		
		final Product<IngestionEvent> product = new Product<>();
		product.setFamily(ProductFamily.AUXILIARY_FILE);
		product.setDto(dto);
			
		final List<Product<IngestionEvent>> products = new ArrayList<>();
		products.add(product);
		
		uut.publish(products, message, UUID.randomUUID());
		
		final GenericPublicationMessageDto<? extends AbstractMessage> result = new GenericPublicationMessageDto<>(
				message.getId(), product.getFamily(), product.getDto());
		result.setInputKey(message.getInputKey());
		result.setOutputKey(product.getFamily().toString());
		
		verify(mqiClient, times(1)).publish(Mockito.eq(result), Mockito.eq(ProductCategory.INGESTION_EVENT));
	}
	*/
}

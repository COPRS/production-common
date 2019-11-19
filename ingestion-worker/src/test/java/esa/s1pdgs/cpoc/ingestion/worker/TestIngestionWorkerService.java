package esa.s1pdgs.cpoc.ingestion.worker;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.ingestion.worker.IngestionWorkerService;
import esa.s1pdgs.cpoc.ingestion.worker.config.IngestionWorkerServiceConfigurationProperties;
import esa.s1pdgs.cpoc.ingestion.worker.config.IngestionTypeConfiguration;
import esa.s1pdgs.cpoc.ingestion.worker.product.IngestionResult;
import esa.s1pdgs.cpoc.ingestion.worker.product.Product;
import esa.s1pdgs.cpoc.ingestion.worker.product.ProductException;
import esa.s1pdgs.cpoc.ingestion.worker.product.ProductService;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractDto;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;

public final class TestIngestionWorkerService {
	
	@Mock
	GenericMqiClient mqiClient;
	
	@Mock
	Reporting.Factory reportingFactory;
	
	@Mock
	Logger logger;
	
	@Mock
	ProductService productService;
	
	@Mock
	ErrorRepoAppender errorRepoAppender;
	
	@Before
	public final void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public final void testOnMessage() {
		final IngestionJob ingestion = new IngestionJob("fooBar");
		ingestion.setRelativePath("fooBar");
		ingestion.setPickupPath("/tmp");
		
		final GenericMessageDto<IngestionJob> mess = new GenericMessageDto<IngestionJob>();
		mess.setId(123);
		mess.setInputKey("testKEy");
		mess.setBody(ingestion);
		
		final ProductService fakeProductService = new ProductService() {			
			@Override
			public void markInvalid(IngestionJob ingestion) {}
			
			@Override
			public IngestionResult ingest(ProductFamily family, IngestionJob ingestion)
					throws ProductException, InternalErrorException {
				return IngestionResult.NULL;
			}
		};		
		final IngestionWorkerService uut = new IngestionWorkerService(
				null, 
				ErrorRepoAppender.NULL, 
				new IngestionWorkerServiceConfigurationProperties(), 
				fakeProductService
		);
		uut.onMessage(mess);
	}

	@Test
	public final void testIdentifyAndUpload() throws InternalErrorException {
		IngestionWorkerServiceConfigurationProperties properties = new IngestionWorkerServiceConfigurationProperties();
		IngestionTypeConfiguration itc = new IngestionTypeConfiguration();
		itc.setFamily(ProductFamily.AUXILIARY_FILE.name());
		itc.setRegex("fo+\\.bar");
		properties.setTypes(Arrays.asList(itc));
		final IngestionWorkerService uut = new IngestionWorkerService(
				mqiClient, 
				ErrorRepoAppender.NULL, 
				properties,
				productService
		);
		doReturn(new LoggerReporting(logger, "uuid", "actionName", 1)).when(reportingFactory).newReporting(Mockito.eq(1));

		GenericMessageDto<IngestionJob> message = new GenericMessageDto<>();
		message.setId(123L);
		message.setBody(null);
		IngestionJob ingestionJob = new IngestionJob("foo.bar");
		message.setBody(ingestionJob);
		
		File file = new File("foo.bar");
		final Product<AbstractDto> prod = new Product<>();
		prod.setFamily(ProductFamily.AUXILIARY_FILE);
		prod.setFile(file);	
		final ProductionEvent dto = new ProductionEvent(
				file.getName(), 
				"foo.bar", 
				ProductFamily.AUXILIARY_FILE
		);
		prod.setDto(dto);
		
		IngestionResult expectedResult = new IngestionResult(Arrays.asList(prod), 0L);
		doReturn(expectedResult).when(productService).ingest(Mockito.eq(ProductFamily.AUXILIARY_FILE), Mockito.eq(ingestionJob));
		
		IngestionResult result = uut.identifyAndUpload(reportingFactory, message, ingestionJob);
		assertEquals(expectedResult, result);
		verify(productService, times(1)).ingest(Mockito.eq(ProductFamily.AUXILIARY_FILE), Mockito.eq(ingestionJob));
		verify(productService, never()).markInvalid(Mockito.any());
	}
	
	@Test
	public final void testIdentifyAndUploadOnInvalidFamily() throws InternalErrorException {
		IngestionWorkerServiceConfigurationProperties properties = new IngestionWorkerServiceConfigurationProperties();
		IngestionTypeConfiguration itc = new IngestionTypeConfiguration();
		itc.setFamily("FOO");
		itc.setRegex("fo+\\.bar");
		properties.setTypes(Arrays.asList(itc));
		final IngestionWorkerService uut = new IngestionWorkerService(
				mqiClient, 
				ErrorRepoAppender.NULL, 
				properties,
				productService
		);
		doReturn(new LoggerReporting(logger, "uuid", "actionName", 1)).when(reportingFactory).newReporting(Mockito.eq(1));

		GenericMessageDto<IngestionJob> message = new GenericMessageDto<>();
		message.setId(123L);
		message.setBody(null);
		IngestionJob ingestionJob = new IngestionJob("foo.bar");
		message.setBody(ingestionJob);
		
		IngestionResult result = uut.identifyAndUpload(reportingFactory, message, ingestionJob);
		assertEquals(IngestionResult.NULL, result);
		verify(productService, never()).ingest(Mockito.any(), Mockito.any());
		verify(productService, times(1)).markInvalid(Mockito.eq(ingestionJob));
	}

	@Test
	public final void testGetFamilyForNominal() {
		IngestionWorkerServiceConfigurationProperties properties = new IngestionWorkerServiceConfigurationProperties();
		IngestionTypeConfiguration itc = new IngestionTypeConfiguration();
		itc.setFamily(ProductFamily.AUXILIARY_FILE.name());
		itc.setRegex("fo+\\.bar");
		properties.setTypes(Arrays.asList(itc));
		final IngestionWorkerService uut = new IngestionWorkerService(
				mqiClient, 
				ErrorRepoAppender.NULL, 
				properties,
				productService
		);
		
		uut.getFamilyFor(new IngestionJob("foo.bar"));
	}
	
	@Test
	public final void testGetFamilyForNotMatching() {
		IngestionWorkerServiceConfigurationProperties properties = new IngestionWorkerServiceConfigurationProperties();
		IngestionTypeConfiguration itc = new IngestionTypeConfiguration();
		itc.setFamily(ProductFamily.AUXILIARY_FILE.name());
		itc.setRegex("fo+\\.bar");
		properties.setTypes(Arrays.asList(itc));
		final IngestionWorkerService uut = new IngestionWorkerService(
				mqiClient, 
				ErrorRepoAppender.NULL, 
				properties,
				productService
		);
		
		assertThatThrownBy(() -> uut.getFamilyFor(new IngestionJob("fu.bar")))
			.isInstanceOf(ProductException.class)
			.hasMessageContaining("No matching config found for IngestionJob");
	}
	
	@Test
	public final void testGetFamilyForInvalid() {
		IngestionWorkerServiceConfigurationProperties properties = new IngestionWorkerServiceConfigurationProperties();
		IngestionTypeConfiguration itc = new IngestionTypeConfiguration();
		itc.setFamily("FOO");
		itc.setRegex("fo+\\.bar");
		properties.setTypes(Arrays.asList(itc));
		final IngestionWorkerService uut = new IngestionWorkerService(
				mqiClient, 
				ErrorRepoAppender.NULL, 
				properties,
				productService
		);
		
		assertThatThrownBy(() -> uut.getFamilyFor(new IngestionJob("foo.bar")))
			.isInstanceOf(ProductException.class)
			.hasMessageContaining("Invalid IngestionTypeConfiguration [family=FOO, regex=fo+\\.bar] for IngestionJob [");
	}
	
	@Test
	public final void testPublish() throws AbstractCodedException {
		final IngestionWorkerService uut = new IngestionWorkerService(
				mqiClient, 
				ErrorRepoAppender.NULL, 
				new IngestionWorkerServiceConfigurationProperties(),
				productService
		);
		
		doReturn(new LoggerReporting(logger, "uuid", "actionName", 2)).when(reportingFactory).newReporting(Mockito.eq(2));
		
		final GenericMessageDto<IngestionJob> message = new GenericMessageDto<>();
		message.setId(123L);
		message.setInputKey("inputKey");
		message.setBody(new IngestionJob());
		
		AbstractDto dto = new ProductionEvent();
		
		final Product<AbstractDto> product = new Product<>();
		product.setFamily(ProductFamily.AUXILIARY_FILE);
		product.setDto(dto);
			
		final List<Product<AbstractDto>> products = new ArrayList<>();
		products.add(product);
		
		uut.publish(products, message, reportingFactory);
		
		final GenericPublicationMessageDto<? extends AbstractDto> result = new GenericPublicationMessageDto<>(
				message.getId(), product.getFamily(), product.getDto());
		result.setInputKey(message.getInputKey());
		result.setOutputKey(product.getFamily().toString());
		
		verify(mqiClient, times(1)).publish(Mockito.eq(result), Mockito.eq(ProductCategory.AUXILIARY_FILES));
	}
}

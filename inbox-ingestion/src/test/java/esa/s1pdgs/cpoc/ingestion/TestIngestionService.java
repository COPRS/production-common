package esa.s1pdgs.cpoc.ingestion;

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
import esa.s1pdgs.cpoc.ingestion.config.IngestionServiceConfigurationProperties;
import esa.s1pdgs.cpoc.ingestion.config.IngestionTypeConfiguration;
import esa.s1pdgs.cpoc.ingestion.product.IngestionResult;
import esa.s1pdgs.cpoc.ingestion.product.Product;
import esa.s1pdgs.cpoc.ingestion.product.ProductException;
import esa.s1pdgs.cpoc.ingestion.product.ProductService;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractDto;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionDto;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;

public final class TestIngestionService {
	
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
		final IngestionDto ingestion = new IngestionDto("fooBar");
		ingestion.setRelativePath("fooBar");
		ingestion.setPickupPath("/tmp");
		
		final GenericMessageDto<IngestionDto> mess = new GenericMessageDto<IngestionDto>();
		mess.setIdentifier(123);
		mess.setInputKey("testKEy");
		mess.setBody(ingestion);
		
		final ProductService fakeProductService = new ProductService() {			
			@Override
			public void markInvalid(IngestionDto ingestion) {}
			
			@Override
			public IngestionResult ingest(ProductFamily family, IngestionDto ingestion)
					throws ProductException, InternalErrorException {
				return IngestionResult.NULL;
			}
		};		
		final IngestionService uut = new IngestionService(
				null, 
				ErrorRepoAppender.NULL, 
				new IngestionServiceConfigurationProperties(), 
				fakeProductService
		);
		uut.onMessage(mess);
	}

	@Test
	public final void testIdentifyAndUpload() throws InternalErrorException {
		IngestionServiceConfigurationProperties properties = new IngestionServiceConfigurationProperties();
		IngestionTypeConfiguration itc = new IngestionTypeConfiguration();
		itc.setFamily(ProductFamily.AUXILIARY_FILE.name());
		itc.setRegex("fo+\\.bar");
		properties.setTypes(Arrays.asList(itc));
		final IngestionService uut = new IngestionService(
				mqiClient, 
				ErrorRepoAppender.NULL, 
				properties,
				productService
		);
		doReturn(new LoggerReporting(logger, "uuid", "actionName", 1)).when(reportingFactory).newReporting(Mockito.eq(1));

		GenericMessageDto<IngestionDto> message = new GenericMessageDto<>();
		message.setIdentifier(123L);
		message.setBody(null);
		IngestionDto ingestionDto = new IngestionDto("foo.bar");
		message.setBody(ingestionDto);
		
		File file = new File("foo.bar");
		final Product<AbstractDto> prod = new Product<>();
		prod.setFamily(ProductFamily.AUXILIARY_FILE);
		prod.setFile(file);	
		final ProductDto dto = new ProductDto(
				file.getName(), 
				"foo.bar", 
				ProductFamily.AUXILIARY_FILE
		);
		prod.setDto(dto);
		
		IngestionResult expectedResult = new IngestionResult(Arrays.asList(prod), 0L);
		doReturn(expectedResult).when(productService).ingest(Mockito.eq(ProductFamily.AUXILIARY_FILE), Mockito.eq(ingestionDto));
		
		IngestionResult result = uut.identifyAndUpload(reportingFactory, message, ingestionDto);
		assertEquals(expectedResult, result);
		verify(productService, times(1)).ingest(Mockito.eq(ProductFamily.AUXILIARY_FILE), Mockito.eq(ingestionDto));
		verify(productService, never()).markInvalid(Mockito.any());
	}
	
	@Test
	public final void testIdentifyAndUploadOnInvalidFamily() throws InternalErrorException {
		IngestionServiceConfigurationProperties properties = new IngestionServiceConfigurationProperties();
		IngestionTypeConfiguration itc = new IngestionTypeConfiguration();
		itc.setFamily("FOO");
		itc.setRegex("fo+\\.bar");
		properties.setTypes(Arrays.asList(itc));
		final IngestionService uut = new IngestionService(
				mqiClient, 
				ErrorRepoAppender.NULL, 
				properties,
				productService
		);
		doReturn(new LoggerReporting(logger, "uuid", "actionName", 1)).when(reportingFactory).newReporting(Mockito.eq(1));

		GenericMessageDto<IngestionDto> message = new GenericMessageDto<>();
		message.setIdentifier(123L);
		message.setBody(null);
		IngestionDto ingestionDto = new IngestionDto("foo.bar");
		message.setBody(ingestionDto);
		
		IngestionResult result = uut.identifyAndUpload(reportingFactory, message, ingestionDto);
		assertEquals(IngestionResult.NULL, result);
		verify(productService, never()).ingest(Mockito.any(), Mockito.any());
		verify(productService, times(1)).markInvalid(Mockito.eq(ingestionDto));
	}

	@Test
	public final void testGetFamilyForNominal() {
		IngestionServiceConfigurationProperties properties = new IngestionServiceConfigurationProperties();
		IngestionTypeConfiguration itc = new IngestionTypeConfiguration();
		itc.setFamily(ProductFamily.AUXILIARY_FILE.name());
		itc.setRegex("fo+\\.bar");
		properties.setTypes(Arrays.asList(itc));
		final IngestionService uut = new IngestionService(
				mqiClient, 
				ErrorRepoAppender.NULL, 
				properties,
				productService
		);
		
		uut.getFamilyFor(new IngestionDto("foo.bar"));
	}
	
	@Test
	public final void testGetFamilyForNotMatching() {
		IngestionServiceConfigurationProperties properties = new IngestionServiceConfigurationProperties();
		IngestionTypeConfiguration itc = new IngestionTypeConfiguration();
		itc.setFamily(ProductFamily.AUXILIARY_FILE.name());
		itc.setRegex("fo+\\.bar");
		properties.setTypes(Arrays.asList(itc));
		final IngestionService uut = new IngestionService(
				mqiClient, 
				ErrorRepoAppender.NULL, 
				properties,
				productService
		);
		
		assertThatThrownBy(() -> uut.getFamilyFor(new IngestionDto("fu.bar")))
			.isInstanceOf(ProductException.class)
			.hasMessageContaining("No matching config found for IngestionDto");
	}
	
	@Test
	public final void testGetFamilyForInvalid() {
		IngestionServiceConfigurationProperties properties = new IngestionServiceConfigurationProperties();
		IngestionTypeConfiguration itc = new IngestionTypeConfiguration();
		itc.setFamily("FOO");
		itc.setRegex("fo+\\.bar");
		properties.setTypes(Arrays.asList(itc));
		final IngestionService uut = new IngestionService(
				mqiClient, 
				ErrorRepoAppender.NULL, 
				properties,
				productService
		);
		
		assertThatThrownBy(() -> uut.getFamilyFor(new IngestionDto("foo.bar")))
			.isInstanceOf(ProductException.class)
			.hasMessageContaining("Invalid IngestionTypeConfiguration [family=FOO, regex=fo+\\.bar] for IngestionDto [");
	}
	
	@Test
	public final void testPublish() throws AbstractCodedException {
		final IngestionService uut = new IngestionService(
				mqiClient, 
				ErrorRepoAppender.NULL, 
				new IngestionServiceConfigurationProperties(),
				productService
		);
		
		doReturn(new LoggerReporting(logger, "uuid", "actionName", 2)).when(reportingFactory).newReporting(Mockito.eq(2));
		
		final GenericMessageDto<IngestionDto> message = new GenericMessageDto<>();
		message.setIdentifier(123L);
		message.setInputKey("inputKey");
		message.setBody(new IngestionDto());
		
		AbstractDto dto = new ProductDto();
		
		final Product<AbstractDto> product = new Product<>();
		product.setFamily(ProductFamily.AUXILIARY_FILE);
		product.setDto(dto);
			
		final List<Product<AbstractDto>> products = new ArrayList<>();
		products.add(product);
		
		uut.publish(products, message, reportingFactory);
		
		final GenericPublicationMessageDto<? extends AbstractDto> result = new GenericPublicationMessageDto<>(
				message.getIdentifier(), product.getFamily(), product.getDto());
		result.setInputKey(message.getInputKey());
		result.setOutputKey(product.getFamily().toString());
		
		verify(mqiClient, times(1)).publish(Mockito.eq(result), Mockito.eq(ProductCategory.AUXILIARY_FILES));
	}
}
